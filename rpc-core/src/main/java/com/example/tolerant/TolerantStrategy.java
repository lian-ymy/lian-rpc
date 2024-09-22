package com.example.tolerant;

import com.example.model.RpcResponse;

import java.util.Map;

/**
 * 容错策略接口
 */
public interface TolerantStrategy {

    /**
     * 容错
     * @param context 上下文，用于传递数据
     * @param e 实际异常
     * @return
     */
    RpcResponse doTolerant(Map<String,Object> context,Exception e);
}