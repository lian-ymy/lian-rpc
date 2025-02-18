package com.example.registry.updater;

import com.example.model.ServiceMetaInfo;
import com.example.registry.Registry;
import com.example.registry.RegistryFactory;
import com.example.rpc.RpcApplication;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 配合负载均衡规则 - 权重轮询使用
 * 各个服务节点更新在内存当前权重(currentWeight)之后
 * 通过相应的注册中心 Client 完成服务信息注册
 */
@Slf4j
public class ServiceMetaInfoUpdater {

    // 创建线程池
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * 异步更新服务到注册中心
     *
     * @param list 要更新的服务列表
     * @return
     */
    public static CompletableFuture<Void> asynUpdateServiceMetaInfos(List<ServiceMetaInfo> list) {
        // 获取注册中心实例
        Registry registry = RegistryFactory.getRegistry(
                RpcApplication.getRpcConfig().getRegistryConfig().getRegistryType()
        );

        //将每个服务的注册操作(更新当前服务的权重 currentWeight )包装成异步任务
        List<CompletableFuture<Void>> futures = list.stream().map(
                info -> CompletableFuture.runAsync(() -> {
                    try {
                        registry.registry(info);
                    } catch (Exception e) {
                        log.debug("Error when update ServiceMetaInfo key:{}, message:{}",
                                info.getServiceNodeKey(),
                                e.getMessage());
                        throw new RuntimeException(e);
                    }
                }, executor)
        ).toList();
        // 等待所有任务完成
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

}
