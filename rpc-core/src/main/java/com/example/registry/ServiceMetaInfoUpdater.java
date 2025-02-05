package com.example.registry;

import com.example.model.ServiceMetaInfo;
import com.example.rpc.RpcApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
public class ServiceMetaInfoUpdater {

    /**
     * 用于更新注册节点信息的线程池，减少了线程的创建和销毁
     */
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10) ;
    private static final Logger log = LoggerFactory.getLogger(ServiceMetaInfoUpdater.class);

    public static CompletableFuture<Void> asyncUpdateServiceMetaInfo(List<ServiceMetaInfo> serviceMetaInfoList) {
        //获取注册中心
        Registry registry = RegistryFactory.getInstance(RpcApplication
                .getRpcConfig()
                .getRegistryConfig()
                .getRegistry());

        //将每个服务的注册操作放入线程池
        List<CompletableFuture<Void>> futures = serviceMetaInfoList.stream()
                .map(serviceMetaInfo -> CompletableFuture.runAsync(() -> {
                    try {
                        registry.register(serviceMetaInfo);
                    } catch (Exception e) {
                        log.debug("Error when updating service meta info key:{}, message:{}",
                                serviceMetaInfo.getServiceKey(), e);
                        throw new RuntimeException(e);
                    }
                }, executorService)).toList();

        //等待所有线程执行完毕
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
