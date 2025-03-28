package org.provider;

import org.common.RpcRequestHolder;
import org.common.constants.MsgType;
import org.common.constants.ProtocolConstants;
import org.junit.jupiter.api.Test;
import org.socket.codec.MsgHeader;
import org.socket.codec.RpcProtocol;
import org.socket.codec.RpcRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.socket.serialization.hessian.HessianSerialization;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ProviderApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) throws IOException{
        final HessianSerialization serialization = new HessianSerialization();

        final MyObject myObject = new MyObject();

        int count =100000;

        final RpcProtocol rpcProtocol = new RpcProtocol();

        //构建消息头
        MsgHeader header = new MsgHeader();
        long requestId = RpcRequestHolder.getRequestId();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        header.setMsgType((byte) MsgType.REQUEST.ordinal());
        header.setStatus((byte) 0x1);
        rpcProtocol.setHeader(header);

        final RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setClassName("org.HelloService");
        rpcRequest.setMethodCode(1230);
        rpcRequest.setMethodName("method");
        rpcRequest.setServiceVersion("1.0");
        rpcRequest.setParameterTypes(MyObject.class);
        rpcRequest.setParameter(myObject);

        rpcProtocol.setBody(rpcRequest);

        final byte[] data = serialization.serialize(rpcProtocol);
        Long start = System.currentTimeMillis();
        for(int i=0;i<count;i++){
            serialization.deserialize(data,RpcRequest.class);
        }
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

}


class MyObject implements Serializable{
    String name = "wmh";
    Integer age =18;

    List<Integer> ids = new ArrayList<>();

    public MyObject(){
        for(int i=0;i<100;i++){
            ids.add(i);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}