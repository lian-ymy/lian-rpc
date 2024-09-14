package com.example.protocol;

import com.example.serializer.Serializer;
import com.example.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

public class ProtocolMessageEncoder {

    /**
     * 编码
     * @param protocolMessage
     * @return
     */
    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {
        if(protocolMessage == null || protocolMessage.getHeader() == null) {
            return Buffer.buffer();
        }
        ProtocolMessage.Header header = protocolMessage.getHeader();
        //依次向缓冲区写入字节
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());
        //获取序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum
                .getSerializerEnumByKey(header.getSerializer());
        if(serializerEnum == null) {
            throw new RuntimeException("序列化协议不存在！！！");
        }
        //通过序列化器将对象序列化为计算机可以识别的字节类型
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getSerializer());
        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());
        //序列化后，写入body长度和数据体
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);
        return buffer;
    }
}
