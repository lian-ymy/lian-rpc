package com.example.server.tcp;

import com.example.server.HttpServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;

/**
 * tcp服务请求响应处理
 */
public class VertxTcpServer implements HttpServer {

    private byte[] handleRequest(byte[] requestData) {
        //编写处理请求的逻辑，根据requestData构造响应数据并返回
        //示例
        return "Hello,jingliu!".getBytes();
    }

    @Override
    public void doStart(int port) {
        //创建vert.x实例
        Vertx vertx = Vertx.vertx();

        //创建TCP服务器
        NetServer server = vertx.createNetServer();

        //处理请求
        server.connectHandler(socket -> {
           //构造parser先完整读取消息头部
            RecordParser parser = RecordParser.newFixed(8);
            parser.setOutput(new Handler<Buffer>() {
                //初始化
                int size = -1;
                //一次完整的读取（头+体）
                Buffer resultBuffer = Buffer.buffer();

                @Override
                public void handle(Buffer event) {
                    if(-1 == size ) {
                        //从封装的头部信息体中读取消息体长度
                        size = event.getInt(4);
                        parser.fixedSizeMode(size);
                        //写入头信息到结果中
                        resultBuffer.appendBuffer(event);
                    } else {
                        //写入体信息到结果中
                        resultBuffer.appendBuffer(event);
                        System.out.println(resultBuffer.toString());
                        //重置一轮
                        parser.fixedSizeMode(8);
                        size = -1;
                        resultBuffer = Buffer.buffer();
                    }
                }
            });
            socket.handler(parser);
        });

        //启动TCP服务器并监听指定端口
        server.listen(port, result -> {
            if(result.succeeded()) {
                System.out.println("TCP server started on port" + port);
            } else {
                System.out.println("Failed to start TCP server:" + result.cause());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(6666);
    }
}
