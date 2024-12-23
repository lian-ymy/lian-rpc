package com.example.proxy;

import cn.hutool.core.collection.CollUtil;
import com.example.config.RpcConfig;
import com.example.constant.RpcConstant;
import com.example.loadbalancer.LoadBalancer;
import com.example.loadbalancer.LoadBalancerFactory;
import com.example.model.RpcRequest;
import com.example.model.RpcResponse;
import com.example.model.ServiceMetaInfo;
import com.example.registry.Registry;
import com.example.registry.RegistryFactory;
import com.example.retry.RetryFactory;
import com.example.retry.RetryStrategy;
import com.example.rpc.RpcApplication;
import com.example.serializer.Serializer;
import com.example.serializer.SerializerFactory;
import com.example.server.tcp.VertxTcpClient;
import com.example.tolerant.TolerantStrategy;
import com.example.tolerant.TolerantStrategyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务代理(JDK动态代理)
 */
public class ServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     * @param proxy the proxy instance that the method was invoked on
     *
     * @param method the {@code Method} instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the {@code Method} object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or {@code null} if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * {@code java.lang.Integer} or {@code java.lang.Boolean}.
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        //构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        //序列化
        byte[] serialized = serializer.serialize(rpcRequest);
        //发送请求
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(rpcRequest.getServiceName());
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if(CollUtil.isEmpty(serviceMetaInfos)) {
            throw new RuntimeException("暂无服务地址");
        }
        //通过负载均衡器选取指定服务节点进行调用
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        //将调用方法名（请求路径）作为参数调用负载均衡器
        Map<String,Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfos);
        //rpc请求调用，加载指定的重试策略类进行重试判断
        RpcResponse rpcResponse;
        try {
            RetryStrategy retryStrategy = RetryFactory.getInstance(RpcApplication.getRpcConfig().getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() -> {
                return VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            });
        } catch (Exception e) {
            //如果重试后还是抛出异常，就进入容错策略
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null,e);
        }
        return rpcResponse.getData();
    }
}
