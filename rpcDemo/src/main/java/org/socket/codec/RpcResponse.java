package org.socket.codec;

import java.io.Serializable;

public class RpcResponse implements Serializable {
    private Object data;
    private Exception exception;
    public RpcResponse(){}
    @Override
    public String toString() {
        if (exception != null) {
            return "RpcResponse{exception=" + exception.getMessage() + "}";
        } else {
            return "RpcResponse{data=" + data + "}";
        }
    }
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
