import com.example.loadbalancer.LoadBalancer;
import com.example.loadbalancer.LoadBalancerFactory;
import com.example.loadbalancer.LoadBalancerKeys;
import com.example.rpc.RpcApplication;
import org.junit.Test;

import javax.annotation.Resource;

public class TestSingletonSpiLoader {

    @Test
    public void testSpiLoader() {
        RpcApplication.init();
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(LoadBalancerKeys.CONSISTENT_HASH);
        System.out.println(loadBalancer.getClass().getName());
    }
}
