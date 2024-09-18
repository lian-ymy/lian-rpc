package com.example.protocol;

import com.example.model.RpcRequest;
import com.example.model.RpcResponse;
import com.example.serializer.Serializer;
import com.example.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * 协议消息解码器
 */
public class ProtocolMessageDecoder {

    /**
     * 解码
     * @param buffer
     * @return
     */
    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        //分别从指定位置读出buffer的各个参数
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = buffer.getByte(0);
        if(magic != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new RuntimeException("消息magic非法！！！");
        }
        //将读取到的buffer各个参数再填写会到header请求头中
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getByte(5));
        header.setBodyLength(buffer.getByte(13));
        //解决粘包问题，只读取指定长度的数据
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());
        //解析消息体
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum
                .getSerializerEnumByKey(header.getSerializer());
        if(serializerEnum == null) {
            throw new RuntimeException("序列化消息的协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getSerializer());
        //解析具体消息类型（请求还是响应）
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getProtocolMessageTypeByKey(header.getType());
        if(messageTypeEnum == null) {
            throw new RuntimeException("序列化消息对应的类型不存在！！！");
        }
        return switch (messageTypeEnum) {
            case RESPONSE -> {
                RpcResponse rpcResponse = serializer.deserialize(bodyBytes, RpcResponse.class);
                yield new ProtocolMessage<RpcResponse>(header, rpcResponse);
            }
            case REQUEST -> {
                RpcRequest rpcRequest = serializer.deserialize(bodyBytes, RpcRequest.class);
                yield new ProtocolMessage<RpcRequest>(header, rpcRequest);
            }
            default -> throw new RuntimeException("暂不支持当前的消息类型！！！");
        };
    }
}
