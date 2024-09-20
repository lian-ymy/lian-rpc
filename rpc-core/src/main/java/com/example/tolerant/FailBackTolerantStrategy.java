package com.example.tolerant;

import com.example.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 降级到其他服务 - 容错策略
 */
@Slf4j
public class FailBackTolerantStrategy implements TolerantStrategy{

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        return FallBackTolerant();
    }

    public RpcResponse FallBackTolerant() {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setMessage("您要访问的服务不存在，请检查相关信息");
        return rpcResponse;
    }
}
