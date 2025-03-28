package org.common;

import io.netty.util.concurrent.Promise;

/**
 * 处理远程过程调用（RPC）中的异步操作
 * 结合了 Promise<T> 和超时时间 timeout，用于管理 RPC 调用的结果和超时机制
 * @param <T>
 */
public class RpcFuture <T> {
    private Promise<T> promise;  //通常用于表示异步操作的结果。它可能包含成功或失败的结果。
    private long timeout;  //表示 RPC 调用的超时时间

    public Promise<T> getPromise() {
        return promise;
    }

    public void setPromise(Promise<T> promise) {
        this.promise = promise;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public RpcFuture(){

    }

    public RpcFuture(Promise<T> promise,long timeout){
        this.promise = promise;
        this.timeout = timeout;
    }
}
