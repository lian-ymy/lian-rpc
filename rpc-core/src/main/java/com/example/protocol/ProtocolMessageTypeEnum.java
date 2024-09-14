package com.example.protocol;

import lombok.Getter;

/**
 * 协议消息的类型枚举
 */
@Getter
public enum ProtocolMessageTypeEnum {
    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3);

    private final int key;

    ProtocolMessageTypeEnum(int key) {
        this.key = key;
    }

    /**
     * 根据key获取枚举类型
     * @param key
     * @return
     */
    public static ProtocolMessageTypeEnum getProtocolMessageTypeByKey(int key) {
        for (ProtocolMessageTypeEnum typeEnum : ProtocolMessageTypeEnum.values()) {
            if(typeEnum.key == key) {
                return typeEnum;
            }
        }
        return null;
    }
}
