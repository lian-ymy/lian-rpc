package com.example.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.example.config.RegistryConfig;
import com.example.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ETCD注册中心
 */
public class EtcdRegistry implements Registry{
    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * 本机注册的节点key集合（用于维护续期
     */
    private static final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 注册中心服务缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * 正在监听的key集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    private Client client;

    private KV kvClient;

    /**
     * 注册服务
     * @param registryConfig
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder().endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout())).build();
        kvClient = client.getKVClient();
        heartBeat();
    }

    /**
     * 注册服务，创建指定过期时间的租约，并注册到中心中
     * @param serviceMetaInfo
     * @throws Exception
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        //创建租约和KV客户端
        Lease leaseClient = client.getLeaseClient();

        //创建一个30秒的租约
        long leaseId = leaseClient.grant(300).get().getID();

        //设置要存储的键值对
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registryKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        //将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        //添加本地节点到缓存中
        localRegisterNodeKeySet.add(registryKey);
    }

    /**
     * 节点下线，删除对应的键值对
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String serviceKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(serviceKey, StandardCharsets.UTF_8));
        //移除本地节点缓存
        localRegisterNodeKeySet.remove(serviceKey);
    }

    /**
     * 通过服务前缀名搜索对应的服务列表，返回对应的服务列表
     * @param serviceKey
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        //优先从本地缓存中获取服务
        List<ServiceMetaInfo> cachedServiceMetaInfos = registryServiceCache.readCache(serviceKey);
        if(cachedServiceMetaInfos != null) {
            return cachedServiceMetaInfos;
        }

        //前缀搜索，结尾一定要加'/'
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            //前缀查询
            GetOption getOptions = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient
                    .get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOptions)
                    .get()
                    .getKvs();
            //解析服务信息
            List<ServiceMetaInfo> serviceMetaInfos = keyValues.stream()
                    .map(
                    keyValue -> {
                        //获取到值并转化为服务对象
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        //监听key的变化
                        watch(key);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }
                    ).collect(Collectors.toList());

            //写入服务缓存
            registryServiceCache.writeCache(serviceKey, serviceMetaInfos);
            return serviceMetaInfos;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败",e);
        }

    }

    /**
     * 注册中心销毁，释放资源
     */
    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        //遍历本节点的所有key
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key,StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "当前节点下线失败");
            }
        }
        //释放资源
        if(kvClient != null) {
            kvClient.close();
        }
        if(client != null) {
            client.close();
        }
    }

    /**
     * 心跳续期机制
     */
    @Override
    public void heartBeat() {
        //10秒续签一次
        CronUtil.schedule("*/10 * * * * * ", (Task) () -> {
            //遍历本节点所有的key
            for (String nodeKey : localRegisterNodeKeySet) {
                try {
                    List<KeyValue> keyValues = kvClient.get(ByteSequence.from(nodeKey, StandardCharsets.UTF_8))
                            .get()
                            .getKvs();
                    //如果为空表明该节点已经下线了，直接跳过
                    if(CollUtil.isEmpty(keyValues)) {
                        continue;
                    }
                    //如果不为空，则重新注册（相当于续签）
                    KeyValue keyValue = keyValues.get(0);
                    String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                    ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                    register(serviceMetaInfo);
                } catch (Exception e) {
                    throw new RuntimeException(nodeKey + "续签失败",e);
                }
            }
        });

        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 服务监听（消费端） 如果服务端有新的注册节点，那么就将新的注册节点添加到缓存中
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        //之前未被监听
        boolean isWatched = watchingKeySet.add(serviceNodeKey);
        if(isWatched) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), watchResponse -> {
                for (WatchEvent event : watchResponse.getEvents()) {
                    switch (event.getEventType()) {
                        //key删除时可触发
                        case DELETE:
                            //清理注册服务缓存
                            registryServiceCache.clearCache(serviceNodeKey);
                            break;
                        case PUT:
                            registryServiceCache.clearCache(serviceNodeKey);
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }
}
