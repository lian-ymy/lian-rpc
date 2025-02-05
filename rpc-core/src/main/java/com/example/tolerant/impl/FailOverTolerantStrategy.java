package com.example.tolerant.impl;

import com.example.model.RpcRequest;
import com.example.model.RpcResponse;
import com.example.model.ServiceMetaInfo;
import com.example.proxy.sender.RequestSender;
import com.example.retry.RetryStrategy;
import com.example.tolerant.TolerantStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 转移到其他服务节点 - 容错策略
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy {
    /**
     * 容错
     * @param context 上下文，用于传递数据
     * @param e 实际异常
     * @return
     */
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        //记录已经访问的服务节点
        ServiceMetaInfo visited = (ServiceMetaInfo) context.get("visited");
        //记录未被访问过的服务
        List<ServiceMetaInfo> services = (List<ServiceMetaInfo>) context.get("services");
        //移除已经访问过的服务
        services.remove(visited);
        //迁移到位访问过的服务
        RetryStrategy retryStrategy = (RetryStrategy) context.get("retryStrategy");
        RequestSender requestSender = (RequestSender) context.get("sender");
        RpcRequest request = (RpcRequest) context.get("rpcRequest");
        for (ServiceMetaInfo serviceInfo : services) {
            log.warn("Fail Over - select service:{}", serviceInfo.getServiceHost());
            try {
                //重试未访问服务节点
                retryStrategy.doRetry(() -> {
                    return requestSender.convertAndSend(serviceInfo.getServiceAddress(), request);
                });
            } catch (Exception ex) {
                //尝试下一个节点
                continue;
            }
        }
        return new RpcResponse();
    }
}
