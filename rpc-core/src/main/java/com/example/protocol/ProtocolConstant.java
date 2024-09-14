package com.example.protocol;

/**
 * 自定义协议类型常量
 */
public interface ProtocolConstant {
    /**
     * 消息头长度
     */
    int MESSAGE_HEADER_LENGTH = 17;

    /**
     * 协议魔数
     */
    byte PROTOCOL_MAGIC = 0x45;

    /**
     * 协议版本号
     */
    byte PROTOCOL_VERSION = 0x1;
}
