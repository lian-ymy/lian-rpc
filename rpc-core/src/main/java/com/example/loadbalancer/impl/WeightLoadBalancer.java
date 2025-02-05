package com.example.loadbalancer.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import com.example.loadbalancer.LoadBalancer;
import com.example.model.ServiceMetaInfo;
import com.example.registry.ServiceMetaInfoUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 * 根据服务权重选择服务能力最好的服务
 * */
public class WeightLoadBalancer implements LoadBalancer {
    private static final Logger log = LoggerFactory.getLogger(WeightLoadBalancer.class);

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        //没有注册节点信息
        if(ObjUtil.isNull(serviceMetaInfoList) || serviceMetaInfoList.isEmpty()) {
            return null;
        }
        //只有一个节点
        if(serviceMetaInfoList.size() == 1) {
            return serviceMetaInfoList.get(0);
        }
        //计算所有服务的权重之和
        int totalWeight = serviceMetaInfoList.stream().mapToInt(ServiceMetaInfo::getServiceWeight).sum();

        //得到当前所有服务节点中服务权重最大的节点
        ServiceMetaInfo selectServiceMeta = serviceMetaInfoList.stream()
                .max(Comparator.comparingInt(ServiceMetaInfo::getCurrentWeight)).get();
        log.debug("select service meta info:{}",selectServiceMeta);

        //更新当前选中节点的服务权重
        selectServiceMeta.setCurrentWeight(selectServiceMeta.getCurrentWeight() - totalWeight);
        log.debug("update service meta info:{}",selectServiceMeta);

        //动态更新所有服务节点的服务权重
        serviceMetaInfoList.forEach(serviceMetaInfo -> {
            serviceMetaInfo.setCurrentWeight(serviceMetaInfo.getCurrentWeight() + serviceMetaInfo.getServiceWeight());
        });

        //异步更新注册节点信息到注册中心
        ServiceMetaInfoUpdater.asyncUpdateServiceMetaInfo(serviceMetaInfoList);

        return selectServiceMeta;
    }
}
