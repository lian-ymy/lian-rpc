package com.example.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.example.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI加载器（支持键值对映射）
 */
@Slf4j
public class SpiLoader {
    /**
     * 存储已加载的类：接口名 => (key => 实现类) 线程安全
     */
    private static Map<String, Map<String,Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例存储(避免重复new),类路径 => 对象实例，单例模式 线程安全
     */
    private static Map<String,Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统SPI目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system";

    /**
     * 用户自定义SPI目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_CUSTOM_SPI_DIR,RPC_SYSTEM_SPI_DIR};

    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     */
    public static void loadAll() {
        for (Class<?> aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }

    /**
     * 获取某个接口的实例
     * @param tClass
     * @param key
     * @return
     * @param <T>
     */
    public static <T> T getInstance(Class<?> tClass, String key) {
        log.info("当前正在获取{}的对应类型",tClass.getName());
        String tClassName = tClass.getName();
        Map<String, Class<?>> stringClassMap = loaderMap.get(tClassName);
        //使用双检锁单例模式实现懒加载
        if(stringClassMap == null) {
            synchronized (SpiLoader.class) {
                if(stringClassMap == null) {
                    load(tClass);
                }
            }
        }
        if(!stringClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在key=%s的类型",tClassName, key));
        }
        //获取到要加载的实现类型
        Class<?> aClass = stringClassMap.get(key);
        //从缓存中加载执行类型的实例
        String implClassName = aClass.getName();
        if(!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName,aClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                String errorMessage = String.format("%s 类实例化失败",implClassName);
                throw new RuntimeException(e);
            }
        }
        return (T) instanceCache.get(implClassName);
    }

    /**
     * 加载某个类型
     * @param loadClass
     * @return
     */
    public static Map<String,Class<?>> load(Class<?> loadClass) {
        //扫描路径，用户自定义的SPI优先级高于系统SPI
        Map<String,Class<?>> keyClassMap = new HashMap<>();
        for (String scanDir : SCAN_DIRS) {
            log.info("{}/{}", scanDir, loadClass.getName());
            List<URL> resources = ResourceUtil.getResources(scanDir + "/" + loadClass.getName());
            log.info("{}",resources);
            //读取每个资源文件
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while((line = bufferedReader.readLine()) != null) {
                        String[] split = line.split("=");
                        if(split.length > 1) {
                            String key = split[0];
                            String className = split[1];
                            keyClassMap.put(key,Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        loaderMap.put(loadClass.getName(),keyClassMap);
        return keyClassMap;
    }
}
