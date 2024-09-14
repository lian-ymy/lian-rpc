package com.example.registry;

import com.example.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心本地缓存
 */
public class RegistryServiceCache {
    /**
     * 服务缓存
     */
    List<ServiceMetaInfo> serviceCache;

    /**
     * 写缓存
     * @param currServiceCache
     */
    void writeCache(List<ServiceMetaInfo> currServiceCache) {
        this.serviceCache = currServiceCache;
    }

    /**
     * 读缓存
     * @return
     */
    List<ServiceMetaInfo> readCache() {
        return this.serviceCache;
    }

    /**
     * 清空缓存
     */
    void clearCache() {
        this.serviceCache = null;
    }
}
