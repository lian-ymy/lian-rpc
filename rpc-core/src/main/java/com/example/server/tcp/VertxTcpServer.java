package com.example.server.tcp;

import com.example.server.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

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
            //处理连接
            socket.handler(buffer -> {
                //处理收到的字节数组
                byte[] requestData = buffer.getBytes();
                //在这里进行自定义的字节数组处理逻辑，解析请求，调用服务，构造响应等
                byte[] responseData = handleRequest(requestData);
                //发送响应
                socket.write(Buffer.buffer(responseData));
                //接收响应
                socket.handler(result -> System.out.println("Received from client: " + result.toString()));
            });
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
