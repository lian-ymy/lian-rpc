package com.example.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mock服务代理
 */
public class MockServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //根据方法的返回值类型，生成特定的默认值对象
        Class<?> methodReturnType = method.getReturnType();
//        log.info("mock invoke{}",method.getName());
        return getDefaultObject(methodReturnType);
    }

    /**
     * 生成指定类型的默认值对象
     * @param type
     * @return
     */
    private Object getDefaultObject(Class<?> type) {
        //基本类型
        if(type.isPrimitive()) {
            if(type == boolean.class) {
                return false;
            } else if(type == short.class) {
                return (short)0;
            } else if(type == int.class) {
                return 0;
            } else if(type == long.class) {
                return 0L;
            } else if(type == String.class) {
                return "今天天气很好，出门走走吧！";
            }
        }
        return null;
    }
}
