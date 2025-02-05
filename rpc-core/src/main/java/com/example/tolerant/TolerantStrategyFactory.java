package com.example.tolerant;

import com.example.spi.SpiLoader;
import com.example.tolerant.impl.FailFastTolerantStrategy;

/**
 * 容错策略产生工厂类
 */
public class TolerantStrategyFactory {

    /**
     * 默认容错策略
     */
    private static final TolerantStrategy DEFAULT_TOLERANT_STRATEGY = new FailFastTolerantStrategy();

    /**
     * 根据键名查找指定的容错策略
     * @param key
     * @return
     */
    public static TolerantStrategy getInstance(String key) {
        return SpiLoader.getInstance(TolerantStrategy.class,key);
    }
}
