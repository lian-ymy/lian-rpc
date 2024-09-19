import com.example.model.RpcResponse;
import com.example.retry.IncreasingTimeRetryStrategy;
import com.example.retry.RetryStrategy;
import org.junit.Test;

/**
 * 重试策略测试
 */
public class RetryStrategyTest {
    RetryStrategy retryStrategy = new IncreasingTimeRetryStrategy();

    @Test
    public void doRetry() {
        try {
            RpcResponse rpcResponse = retryStrategy.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟重试失败");
            });
            System.out.println(rpcResponse);
        } catch (Exception e) {
            System.out.println("重试多次失败");
            throw new RuntimeException(e);
        }

    }
}
