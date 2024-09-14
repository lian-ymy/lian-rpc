package com.example.protocol;

import lombok.Getter;

/**
 * 协议消息的状态枚举
 * 为了使得请求头尽可能小，以键值对的形式保存状态信息的枚举类型
 */
@Getter
public enum ProtocolMessageStatusEnum {
    OK("ok",20),
    BAD_REQUEST("badRequest",40),
    BAD_RESPONSE("badResponse",50);


    private final String text;

    private final int value;

    ProtocolMessageStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static ProtocolMessageStatusEnum getEnumByValue(int value) {
        for (ProtocolMessageStatusEnum statusEnum : ProtocolMessageStatusEnum.values()) {
            if(statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}
