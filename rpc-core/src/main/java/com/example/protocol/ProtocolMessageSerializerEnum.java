package com.example.protocol;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 协议消息的序列化器类型枚举
 *
 * 为了让消息头中存储更少的信息，以不同的数字代表不同的序列化器对应的键，通过键值对存储序列化器信息
 */
@Getter
public enum ProtocolMessageSerializerEnum {
    JDK(1,"jdk"),
    HESSIAN(2,"hessian"),
    KRYO(3,"kryo"),
    JSON(4,"json");

    private final int key;

    private final String serializer;

    ProtocolMessageSerializerEnum(int key, String serializer) {
        this.key = key;
        this.serializer = serializer;
    }

    /**
     * 获取所有序列化器值列表
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(value -> value.serializer).collect(Collectors.toList());
    }

    /**
     * 根据key值获得序列化器类型
     * @param key
     * @return
     */
    public static ProtocolMessageSerializerEnum getSerializerEnumByKey(int key) {
        for (ProtocolMessageSerializerEnum value : values()) {
            if(value.key == key) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据value值获得序列化器类型
     * @param serializer
     * @return
     */
    public static ProtocolMessageSerializerEnum getSerializerEnumByValue(String serializer) {
        for (ProtocolMessageSerializerEnum value : values()) {
            if(value.serializer.equals(serializer)) {
                return value;
            }
        }
        return null;
    }
}
