package org.tolerant;

import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import org.common.*;
import org.socket.codec.RpcResponse;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 定义了一种故障转移（Failover）策略。这种策略的目的是在当前服务节点调用失败时，自动切换到其他可用的服务节点，直到成功为止或所有节点都尝试过。
 */

public class FailoverFaultTolerantStrategy implements FaultTolerantStrategy{
    @Override
    public Object handler(FaultContext faultContext) throws Exception {

        //currentURL 是当前失败的服务节点的 URL。
        //urls 是所有可用服务节点的列表。
        final URL currentURL = faultContext.getCurrentURL();
        final List<URL> urls = faultContext.getUrls();

        //遍历 urls 列表，移除当前失败的节点（currentURL）。
        //这是为了避免再次尝试同一个失败的节点。
        final Iterator<URL> iterator = urls.iterator();
        while(iterator.hasNext()){
            if(iterator.next().equals(currentURL)){
                iterator.remove();
            }
        }

        //检查是否还有可用节点
        if(urls.isEmpty()){
            throw new Exception("服务端发生异常,触发故障容错机制: 故障转移,无服务可用");
        }

        //从 urls 列表中选择第一个可用的节点。
        final URL url = urls.get(0);

        //使用 Netty 的 ChannelFuture 获取与目标节点的连接
        final ChannelFuture channelFuture = Cache.CHANNEL_FUTURE_MAP.get(new Host(url.getIp(), url.getPort()));

        //将 RpcProtocol 对象（封装了 RPC 请求信息）通过 Netty 的 Channel 发送到目标节点
        channelFuture.channel().writeAndFlush(faultContext.getRpcProtocol());

        //注册 RPC 请求并等待响应
        RpcFuture<RpcResponse> future = new RpcFuture(new DefaultPromise(new DefaultEventLoop()),3000);
        RpcRequestHolder.REQUEST_MAP.put(faultContext.getRequestId(), future);
        RpcResponse rpcResponse = future.getPromise().sync().get(future.getTimeout() , TimeUnit.MILLISECONDS);

        //处理响应
        if(rpcResponse.getException() != null){
            faultContext.setCurrentURL(url);
            return handler(faultContext);
        }
        return rpcResponse.getData();
    }
}
