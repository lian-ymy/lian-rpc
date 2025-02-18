package com.example.model;

import cn.hutool.core.util.StrUtil;
import com.example.model.constant.Protocol;
import com.example.model.constant.ServiceWeight;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import lombok.Data;

import java.util.Map;

/**
 * 服务元信息(注册信息)
 */
@Data
public class ServiceMetaInfo {
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号
     */
    private String serviceVersion = "1.0";

    /**
     * 服务域名
     */
    private String serviceHost;

    /**
     * 服务端口号
     */
    private Integer servicePort;

    /**
     * 服务分组
     */
    private String serviceGroup = "default";

    /**
     * 节点注册时间
     */
    private String registerTime = "";

    /**
     * 节点启动时间
     */
    private String startTime = "";

    /**
     * 通信协议，默认 HTTP
     */
    private String protocol = Protocol.HTTP;

    /**
     * 服务权重，默认 0
     */
    private Integer serviceWeight = ServiceWeight.ZERO;

    /**
     * 服务的动态权重
     */
    private Integer currentWeight = ServiceWeight.ZERO;

    /**
     * 自定义元数据
     * 允许用户附加额外的服务信息，便于扩展。
     */
    private Map<String, String> metadata;

    /**
     * 获取服务键名
     * @return
     */
    public String getServiceKey() {
        return String.format("%s:%s",serviceName,serviceVersion);
    }

    /**
     * 获取服务注册节点信息
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s",getServiceKey(),serviceHost, servicePort);
    }

    /**
     * 获取服务地址
     */
    public String getServiceAddress() {
        if(!StrUtil.contains(serviceHost,"http")) {
            return String.format("http://%s:%s",serviceHost, servicePort);
        }
        return String.format("%s:%s",serviceHost, servicePort);
    }
}
