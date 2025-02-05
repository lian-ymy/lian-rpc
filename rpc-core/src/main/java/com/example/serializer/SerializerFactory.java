package com.example.serializer;

import com.example.serializer.impl.JdkSerializer;
import com.example.spi.SpiLoader;

/**
 * 序列化器（用于获取序列化器对象）
 */
public class SerializerFactory {

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class,key);
    }
}
