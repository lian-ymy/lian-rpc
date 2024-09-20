package com.example.rpc.springboot.starter.annotation;

import com.example.rpc.springboot.starter.bootstrap.RpcConsumerBootstrap;
import com.example.rpc.springboot.starter.bootstrap.RpcInitBootstrap;
import com.example.rpc.springboot.starter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用Rpc注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcConsumerBootstrap.class, RpcProviderBootstrap.class})
public @interface EnableRpc {
    /**
     * 需要启动注解
     * @return
     */
    boolean needServer() default true;
}
