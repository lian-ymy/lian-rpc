package com.example.proxy.sender;

import com.example.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求协议简单工厂
 * 版本 3.0 优化
 */
@Slf4j
public class RequestSenderFactory {

    /**
     * 基于自定义 SPI 资源路径加载所有 RequestSender 配置
     * 配置格式: key=RequeSender实现类全类名
     */
    static {
        SpiLoader.load(RequestSender.class);
    }

    public static final RequestSender DEFAULT_REQUEST_SENDER = new TcpRequestSender();

    public static RequestSender getSender(String protocol) {
        return SpiLoader.getInstance(RequestSender.class, protocol);
    }
}
