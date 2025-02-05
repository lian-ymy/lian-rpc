package com.example.utils;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * 配置工具类
 */
public class ConfigUtils {

    /**
     * 加载配置对象
     * @param tClass
     * @param prefix
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }


    /**
     * 加载配置对象，支持区分不同的环境(本地调试环境，线上测试环境)
     * @param tClass
     * @param prefix
     * @param environment
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        StringBuilder properties = configFileBuilder.append(".properties");
        StringBuilder name = configFileBuilder.append(".yml");
        Props props = new Props(properties.toString());
        props.autoLoad(true);

        //解析类
        Yaml yaml = new Yaml(new Constructor(tClass, new LoaderOptions()));

        Object ymlFile;
        try (InputStream in = ConfigUtils.class
                .getClassLoader()
                .getResourceAsStream(name.toString())) {
            if (in == null) {
                throw new RuntimeException("File: " + name.toString() + " NOT FOUND");
            }

            //加载 yaml 格式配置
            ymlFile = yaml.load(in);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Loading .yml/yaml config file fail!! - " + e);
        }

        T propertiesFile = props.toBean(tClass, prefix);

        if (ObjUtil.isNotNull(ymlFile)) {
            return (T) ymlFile;
        } else {
            return propertiesFile;
        }
    }
}
