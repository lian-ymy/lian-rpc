package com.example.registry;

import cn.hutool.core.util.StrUtil;
import com.example.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lian
 * @description: 服务缓存类 - 支持基于 ServiceKey(serviceName:version) 缓存已经发现的服务节点
 */
@Slf4j
public class RegistryServiceCache {

    /**
     * 本地服务缓存，支持基于(serviceKey)多服务注册
     */
    Map<String, List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();

    /**
     * 写缓存
     *
     * @param serviceKey
     * @param list
     */
    void writeCache(String serviceKey, List<ServiceMetaInfo> list) {
        if (StrUtil.isBlank(serviceKey)) {
            throw new RuntimeException("Registry Service Key can not be Empty");
        }
        serviceCache.put(serviceKey, list);
    }

    /**
     * 读缓存 - 基于 serviceKey
     *
     * @param serviceKey 服务键名 (serviceName:serviceVersion)
     */
    List<ServiceMetaInfo> readCache(String serviceKey) {
        if (this.serviceCache.isEmpty()) {
            throw new RuntimeException("Current local service cache `RegistryServiceCache` is Empty!");
        }
        try {
            return serviceCache.get(serviceKey);
        } catch (Exception e) {
            throw new RuntimeException("Not register services contains service key:" + serviceKey);
        }
    }

    /**
     * 清空基于 serviceKey 的缓存
     *
     * @param serviceKey
     */
    public void clear(String serviceKey) {
        try {
            //防止空缓存
            if (!this.serviceCache.containsKey(serviceKey)) {
                return;
            }
            List<ServiceMetaInfo> serviceMetaInfos = this.serviceCache.get(serviceKey);
            serviceMetaInfos.clear();
            log.info("ServiceKey:{} local registered services list is clear", serviceKey);
        } catch (Exception e) {
            log.error("Fail to clear local registered services list ServiceKey:{} ", serviceKey);
            throw new RuntimeException(e);
        }
    }
}
