package org.socket.codec;

import java.io.Serializable;

/**
 * 封装远程过程调用（RPC）协议中的消息格式
 * @param <T>
 */
public class RpcProtocol<T> implements Serializable {
    private MsgHeader header;
    private T body;
    public MsgHeader getHeader() {
        return header;
    }

    public void setHeader(MsgHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
