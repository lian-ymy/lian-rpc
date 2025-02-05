package com.example.serializer.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.example.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian序列化器实现
 */
public class HessianSerializer implements Serializer {
    /**
     * 序列化对象为 Hessian 格式的字节数组
     *
     * @param object 要序列化的对象
     * @return Hessian 格式的字节数组
     * @throws IOException 如果序列化过程中发生错误
     */
    public byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(baos);
        hessian2Output.writeObject(object);
        hessian2Output.flush();
        return baos.toByteArray();
    }

    /**
     * 反序列化 Hessian 格式的字节数组为对象
     *
     * @param bytes  Hessian 格式的字节数组
     * @param clazz  对象的类类型
     * @param <T>   泛型标记
     * @return 反序列化后的对象
     * @throws IOException 如果反序列化过程中发生错误
     */
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Hessian2Input hessian2Input = new Hessian2Input(bais);
        return clazz.cast(hessian2Input.readObject());
    }
}
