package com.example.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

public class VertxTcpClient {
    public void start() {
        //创建Vert.x实例
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(6666, "localhost", result -> {
            if(result.succeeded()) {
                System.out.println("Connected to TCP server");
                NetSocket socket = result.result();
                //发送数据
                for (int i = 0; i < 1000; i++) {
                    Buffer buffer = Buffer.buffer();
                    String str = "Hello, jingliu!Hello, jingliu!Hello, jingliu!Hello, jingliu!";
                    buffer.appendInt(0);
                    buffer.appendInt(str.getBytes().length);
                    buffer.appendBytes(str.getBytes());
                    socket.write(buffer);
                }
                //接收响应
                socket.handler(buffer -> System.out.println("Received response from server" + buffer.toString()));
            } else {
                System.out.println("Failed to connect to TCP server");
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
