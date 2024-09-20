package com.example.tolerant;

import com.example.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 转移到其他服务节点 - 容错策略
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy{
    /**
     * 容错
     * @param context 上下文，用于传递数据
     * @param e 实际异常
     * @return
     */
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        return null;
    }
}
