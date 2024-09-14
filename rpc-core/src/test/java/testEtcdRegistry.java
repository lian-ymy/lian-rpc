import com.example.config.RegistryConfig;
import com.example.model.ServiceMetaInfo;
import com.example.registry.EtcdRegistry;
import com.example.registry.Registry;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * 测试注册中心
 */
public class testEtcdRegistry {
    final Registry registry = new EtcdRegistry();

    @Before
    public void init() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("http://localhost:2379");
        registry.init(registryConfig);
    }

    @Test
    public void register() throws Exception {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("jingliu");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(80);
        registry.register(serviceMetaInfo);
        serviceMetaInfo.setServiceName("jingliu1");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(81);
        registry.register(serviceMetaInfo);
        serviceMetaInfo.setServiceName("jingliu");
        serviceMetaInfo.setServiceVersion("2.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(80);
        registry.register(serviceMetaInfo);
    }

    @Test
    public void search() {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("jingliu");
        serviceMetaInfo.setServiceVersion("1.0");
        List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        System.out.println(serviceMetaInfos.size());
        System.out.println(serviceMetaInfos);
    }

    @Test
    public void unRegister() {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("jingliu1");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(81);
        registry.unRegister(serviceMetaInfo);
    }
}
