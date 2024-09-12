package com.example.server;

import io.vertx.core.Vertx;

import java.io.PrintStream;

/**
 * Http服务实现类
 */
public class VertxHttpServer implements HttpServer{
    @Override
    public void doStart(int port) {
        //创建Vert.x实例
        Vertx vertx = Vertx.vertx();

        //创建Http服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        //监听端口并处理请求
//        server.requestHandler(request -> {
//            //处理Http请求
//            System.out.println("Received request: " + request.method() + " " + request.uri());
//
//            //发送Http响应
//            request.response()
//                    .putHeader("content-type", "text/plain")
//                    .end("Hello from Vert.x Http Server");
//        });
        server.requestHandler(new HttpServerHandler());

        //启动Http服务并监听指定端口
        server.listen(port, httpServerAsyncResult -> {
            if(httpServerAsyncResult.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.out.println("Failed to start server: " + httpServerAsyncResult.cause());
            }
        });
    }
}
