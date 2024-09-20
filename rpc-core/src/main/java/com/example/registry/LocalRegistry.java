package com.example.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地注册中心
 */
public class LocalRegistry {
    /**
     * 注册信息存储
     */
    private static final Map<String,Class<?>> registryMap = new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param name
     * @param implClass
     */
    public static void register(String name, Class implClass) {
        registryMap.put(name,implClass);
    }

    /**
     * 获取服务
     * @param name
     * @return
     */
    public static Class<?> get(String name) {
        return registryMap.get(name);
    }

    /**
     * 删除服务
     * @param name
     */
    public static void remove(String name) {
        registryMap.remove(name);
    }
}
