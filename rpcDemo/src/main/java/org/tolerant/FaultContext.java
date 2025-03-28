package org.tolerant;

import org.common.URL;
import org.socket.codec.RpcProtocol;

import java.util.List;

/**
 * 用于封装故障上下文信息的类
 *
 */
public class FaultContext {

    //当前正在调用的服务节点的 URL。
    private URL currentURL;

    //所有可用的服务节点的 URL 列表
    private List<URL> urls;

    //RPC 协议对象，封装了 RPC 请求的协议信息
    private RpcProtocol rpcProtocol;

    //当前 RPC 请求的唯一标识符
    private Long requestId;

    //在执行 RPC 请求过程中发生的异常
    private Exception exception;
    public FaultContext(){

    }
    public FaultContext(URL currentURL,List<URL> urls,RpcProtocol rpcProtocol,Long requestId,Exception e){
        this.currentURL = currentURL;
        this.urls = urls;
        this.rpcProtocol = rpcProtocol;
        this.requestId = requestId;
        this.exception = e;
    }

    public URL getCurrentURL() {
        return currentURL;
    }

    public void setCurrentURL(URL currentURL) {
        this.currentURL = currentURL;
    }

    public List<URL> getUrls() {
        return urls;
    }

    public void setUrls(List<URL> urls) {
        this.urls = urls;
    }

    public RpcProtocol getRpcProtocol() {
        return rpcProtocol;
    }

    public void setRpcProtocol(RpcProtocol rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
