package com.example.server.tcp;

import io.vertx.core.Vertx;
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
                socket.write("Hello,kafuka!");
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
