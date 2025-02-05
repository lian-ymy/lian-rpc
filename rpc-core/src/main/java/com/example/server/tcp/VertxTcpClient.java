package com.example.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.example.model.RpcRequest;
import com.example.model.RpcResponse;
import com.example.model.ServiceMetaInfo;
import com.example.protocol.*;
import com.example.rpc.RpcApplication;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Vertx TCP请求客户端
 */
public class VertxTcpClient {

    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo selectedServiceMetaInfo) throws ExecutionException, InterruptedException {
        //发送TCP请求
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> completableFuture = new CompletableFuture<>();
        netClient.connect(selectedServiceMetaInfo.getServicePort(),selectedServiceMetaInfo.getServiceHost(),
                handler -> {
                    if(handler.succeeded()) {
                        System.out.println("Connected to TCP Server!");
                        NetSocket socket = handler.result();
                        //发送数据，构造消息
                        ProtocolMessage<Object> protocolMessage = new ProtocolMessage<>();
                        ProtocolMessage.Header header = new ProtocolMessage.Header();
                        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                        header.setSerializer((byte) ProtocolMessageSerializerEnum
                                .getSerializerEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                        //生成全局请求ID
                        header.setRequestId(IdUtil.getSnowflakeNextId());
                        protocolMessage.setHeader(header);
                        //编码请求
                        try {
                            Buffer encode = ProtocolMessageEncoder.encode(protocolMessage);
                            socket.write(encode);
                        } catch (IOException e) {
                            throw new RuntimeException("协议消息编码错误！");
                        }

                        //接受响应
                        socket.handler(buffer -> {
                            try {
                                ProtocolMessage<RpcResponse> responseProtocolMessage =
                                        (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                completableFuture.complete(responseProtocolMessage.getBody());
                            } catch (IOException e) {
                                throw new RuntimeException("协议消息解码错误");
                            }
                        });

                    } else {
                        System.err.println("Failed to connect to TCP Server!");
                    }
                });
        RpcResponse rpcResponse = completableFuture.get();
        //记得关闭连接
        netClient.close();
        return rpcResponse;
    }
}
