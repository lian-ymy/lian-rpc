package com.example.registry;

import com.example.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册中心本地缓存（当前缓存仅限于单机，可扩展为多机部署的缓存系统）
 * 多机部署的缓存系统可通过给每个服务添加唯一标识作为前缀进行匹配
 */
public class RegistryServiceCache {
    /**
     * 服务缓存，用ConcurrentHashMap存储，线程安全
     */
    Map<String,List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();

    /**
     * 写缓存，将上一次的查询进行缓存
     * @param key
     * @param serviceMetaInfos
     */
    void writeCache(String key, List<ServiceMetaInfo> serviceMetaInfos) {
        serviceCache.put(key, serviceMetaInfos);
    }

    /**
     * 读缓存
     * @param key
     * @return
     */
    List<ServiceMetaInfo> readCache(String key) {
        return this.serviceCache.get(key);
    }

    /**
     * 清空缓存
     */
    void clearCache(String key) {
        this.serviceCache.remove(key);
    }
}
