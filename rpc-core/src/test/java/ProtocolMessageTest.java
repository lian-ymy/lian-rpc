import cn.hutool.core.util.IdUtil;
import com.example.model.RpcRequest;
import com.example.protocol.*;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;

import java.io.IOException;

public class ProtocolMessageTest {
    @Test
    public void testEncodeAndDecode() throws IOException {
        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion((byte)0);
        header.setSerializer((byte) ProtocolMessageSerializerEnum.JSON.getKey());
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
        header.setRequestId(IdUtil.getSnowflakeNextId());
        header.setBodyLength(0);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("jingliu");
        rpcRequest.setMethodName("kafuka");
        rpcRequest.setParameterTypes(new Class[]{String.class});
        rpcRequest.setArgs(new Object[]{"heitiane","ruanmei"});
        protocolMessage.setBody(rpcRequest);
        protocolMessage.setHeader(header);

        Buffer encode = ProtocolMessageEncoder.encode(protocolMessage);
        System.out.println(ProtocolMessageDecoder.decode(encode));
    }
}
