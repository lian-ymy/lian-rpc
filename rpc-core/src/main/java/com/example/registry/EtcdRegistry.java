package com.example.registry;

import cn.hutool.json.JSONUtil;
import com.example.config.RegistryConfig;
import com.example.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * ETCD注册中心
 */
public class EtcdRegistry implements Registry{
    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

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
        kvClient.put(key, value, putOption);
    }

    /**
     * 节点下线，删除对应的键值对
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
    }

    /**
     * 通过服务前缀名搜索对应的服务列表，返回对应的服务列表
     * @param serviceKey
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
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
            return keyValues.stream().map(
                    keyValue -> {
                        //获取到值并转化为服务对象
                        String value = keyValue.getValue().toString();
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }
            ).collect(Collectors.toList());
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
        if(kvClient != null) {
            kvClient.close();
        }
        if(client != null) {
            client.close();
        }
    }
}
