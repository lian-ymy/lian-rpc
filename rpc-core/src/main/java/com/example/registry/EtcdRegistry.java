package com.example.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.example.config.RegistryConfig;
import com.example.model.ServiceMetaInfo;
import com.example.registry.strategy.EtcdWatchStrategy;
import com.example.registry.strategy.WatchStrategy;
import com.example.rpc.RpcApplication;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.grpc.StatusRuntimeException;
import io.vertx.core.impl.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class EtcdRegistry implements Registry {

    /**
     * 注册服务的根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";
    /**
     * 是否已启动心跳检测 - 默认 false
     */
    private static boolean isHeartBeatScheduled = false;
    /**
     * 本地注册服务节点(ServiceNodeKey)信息
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    /**
     * 注册中心被监听的服务
     */
    private final Set<String> watchServiceKeySet = new ConcurrentHashSet<>();

    /**
     * 注册中心服务缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();
    /**
     * 关闭心跳检测
     */
    private final Object lock = new Object();
    /**
     * Etcd 客户端
     */
    private Client client;
    /**
     * 键值对操作客户端
     */
    private KV kvClient;
    /**
     * 监控策略
     */
    private WatchStrategy watchStrategy;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // create client using endpoints
        Client client = Client.builder()
                .endpoints("http://localhost:2379")
                .build();

        KV kvClient = client.getKVClient();
        ByteSequence key = ByteSequence.from("test_key".getBytes());
        ByteSequence value = ByteSequence.from("test_value".getBytes());

        // put the key-value
        kvClient.put(key, value).get();

        // get the CompletableFuture
        CompletableFuture<GetResponse> getFuture = kvClient.get(key);

        // get the value from CompletableFuture
        GetResponse response = getFuture.get();

        // delete the key
        kvClient.delete(key).get();
    }

    public void setWatchStrategy(WatchStrategy watchStrategy) {
        this.watchStrategy = watchStrategy;
    }

    @Override
    public void init(RegistryConfig registryConfig) {
        //基于配置中ip+端口+过期时间创建 client
        this.client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();

        //创建 kv 客户端
        this.kvClient = this.client.getKVClient();

        //启动心跳检测机制
        if (RpcApplication.needServer) {
            this.heartBeat();
        }

        //设置监听策略 - Etcd
        if (ObjectUtil.isNull(watchStrategy)) {
            setWatchStrategy(new EtcdWatchStrategy(this.client.getWatchClient()));
            ((EtcdWatchStrategy) watchStrategy).setWatchClient(client.getWatchClient());
            ((EtcdWatchStrategy) watchStrategy).setRegistryServiceCache(this.registryServiceCache);
            ((EtcdWatchStrategy) watchStrategy).setWatchServiceKeySet(this.watchServiceKeySet);
        }
    }

    @Override
    public void heartBeat() {
        if (isHeartBeatScheduled) {
            log.warn("HeartBeat task is already scheduled.");
            return;
        }

        //无需要心跳检测的服务节点
        if (this.localRegisterNodeKeySet.isEmpty()) {
            return;
        }

        //10s 续签一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                for (String nodeKey : localRegisterNodeKeySet) {
                    List<KeyValue> keyValues;
                    try {
                        //查询该服务节点
                        keyValues = kvClient.get(ByteSequence.from(nodeKey, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();

                        // 节点已经过期，需要重启节点才能重新注册
                        if (CollUtil.isEmpty(keyValues)) {
                            continue;
                        }

                        //节点为过期(相当于续签)
                        //基于服务节点查询，仅得到唯一结果
                        KeyValue keyValue = keyValues.get(0);

                        //服务向 Etcd 注册时使用 Json 格式
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);

                        //续约
                        registry(serviceMetaInfo);

                    } catch (StatusRuntimeException e) {
                        // 仅忽略 INTERNAL 错误码的异常，其他异常仍需处理
                        if (e.getStatus().getCode() == io.grpc.Status.Code.INTERNAL) {
                            log.warn("Ignored INTERNAL error during service node renewal for {}: {}", nodeKey, e.getMessage());
                        }
                    } catch (Exception e) {
                        log.error("Error occurred during service node renewal for {}: {}", nodeKey, e.getMessage());
                    }
                }
            }
        });

        CronUtil.setMatchSecond(true);
        // 仅在未启动时启动调度器
        if (!CronUtil.getScheduler().isStarted()) {
            CronUtil.start();
            isHeartBeatScheduled = true;
        }
    }

    @Override
    public void watch(String serviceNodeKey) {
        log.info("Start watching service node key:{}, using strategy:{}",
                serviceNodeKey,
                watchStrategy.getClass().getSimpleName());
        watchStrategy.watch(serviceNodeKey);
    }

    @Override
    public boolean registry(ServiceMetaInfo serviceMetaInfo) {
        //创建 Lease 客户端
        Lease leaseClient = client.getLeaseClient();

        //租约 Id
        long leaseId;
        //注册的 key 值
        String registryKey;

        //转化为字节流 k - v
        ByteSequence key;
        ByteSequence value;

        try {
            //30s 租约
            leaseId = leaseClient.grant(30).get().getID();
            //记录注册时间
            setRegistryTimeDate(serviceMetaInfo);
            //设置要存储的服务键值: /rpc/ + ServiceNode(服务名:版本号/IP:Port)
            registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
            key = ByteSequence.from(registryKey, StandardCharsets.UTF_8);
            value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

            // 将键值对与租约关联起来，并且设置过期时间
            PutOption putOption = PutOption
                    .builder()
                    .withLeaseId(leaseId)
                    .build();
            //执行 put 操作
            kvClient.put(key, value, putOption).get();
            log.info("Register Service Node key:{}", serviceMetaInfo.getServiceNodeKey());
            //存储该服务节点key
            this.localRegisterNodeKeySet.add(registryKey);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean unRegistry(ServiceMetaInfo serviceMetaInfo) {
        String serviceNodeKey = serviceMetaInfo.getServiceNodeKey();
        //删除 - 基于 ServiceNode (service name + service version + host address + port)
        try {
            this.kvClient.delete(ByteSequence.from(
                    ETCD_ROOT_PATH + serviceMetaInfo,
                    StandardCharsets.UTF_8));
            //删除存储在本地服务的节点 key
            this.localRegisterNodeKeySet.remove(ETCD_ROOT_PATH + serviceNodeKey);
            //更新缓存
            this.registryServiceCache.clear(ETCD_ROOT_PATH + serviceMetaInfo.getServiceKey());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {

        String searchKey = ETCD_ROOT_PATH + serviceKey;
        //先查询缓存
        Map<String, List<ServiceMetaInfo>> serviceCache = this.registryServiceCache.serviceCache;
        if (serviceCache.containsKey(searchKey) && serviceCache.get(searchKey).size() != 0) {
            log.info("ServiceKey:{} hit Registry Service Cache, read data from Cache", searchKey);
            return registryServiceCache.readCache(searchKey);
        }

        //基于前缀索引搜索
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        GetOption getOption = GetOption.builder().isPrefix(true).build();
        try {
            List<KeyValue> keyValueList = kvClient.get(
                    ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                    getOption
            ).get().getKvs();

            //解析服务名称
            List<ServiceMetaInfo> serviceMetaInfos = keyValueList
                    .stream()
                    .map((kv) -> {
                        //监听key
                        String key = kv.getKey().toString(StandardCharsets.UTF_8);
                        //监控 ETCD_ROOT_PATH + serviceKey 子串
                        watch(key);
                        String value = kv.getValue().toString(StandardCharsets.UTF_8);
                        //映射成 ServiceMetaInfo
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }).collect(Collectors.toList());

            //更新缓存
            this.registryServiceCache.writeCache(searchKey, serviceMetaInfos);

            return serviceMetaInfos;
        } catch (Exception e) {
            log.error("Service discovery fail, read from cache instead:{}", e.getMessage());
            //服务发现异常,本地缓存兜底
            if (serviceCache.containsKey(searchKey) && serviceCache.get(searchKey).size() != 0) {
                log.info("ServiceKey:{} read from Cache", searchKey);
                return registryServiceCache.readCache(searchKey);
            } else {
                return new ArrayList<>();
            }
        }
    }

    @Override
    public void destory() {

        //下线本地保存的注册节点信息
        for (String nodeKey : localRegisterNodeKeySet) {
            try {
                this.kvClient.delete(ByteSequence.from(nodeKey, StandardCharsets.UTF_8));
                log.info("Service key node" + nodeKey + " has been destoryed;");
                this.registryServiceCache.clear(nodeKey);
            } catch (Exception e) {
                throw new RuntimeException("Service key node: " + nodeKey + " destroy fail!");
            }
        }

        this.localRegisterNodeKeySet.clear();
        watchServiceKeySet.clear();

        if (this.kvClient != null) {
            this.kvClient.close();
        }
        if (this.client != null) {
            this.client.close();
        }

        // 停止心跳检测
        if (CronUtil.getScheduler().isStarted()) {
            synchronized (lock) {
                if (CronUtil.getScheduler().isStarted()) { // 双重检查
                    CronUtil.stop();
                }
            }
        }
    }
}
