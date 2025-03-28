package org.proxy.cglib;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.annotation.RpcReference;
import org.common.*;
import org.common.constants.*;
import org.config.Properties;
import org.filter.*;
import org.register.RegistryFactory;
import org.router.LoadBalancer;
import org.router.LoadBalancerFactory;
import org.socket.codec.MsgHeader;
import org.socket.codec.RpcProtocol;
import org.socket.codec.RpcRequest;
import org.socket.codec.RpcResponse;
import org.tolerant.FaultContext;
import org.tolerant.FaultTolerantStrategy;
import org.tolerant.FaultTolerantFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 实现了一个基于 CGLIB 的动态代理类 CgLibProxy，它实现了 MethodInterceptor 接口。
 * 主要功能是拦截目标对象的方法调用，并通过 RPC（远程过程调用）的方式将请求发送到远程服务，等待响应并返回结果。
 * 步骤：
 * 拦截目标对象的方法调用：通过CGLIB动态代理。
 * 构建RPC请求：包括消息头和请求主体。
 * 服务发现：从注册中心获取目标服务的地址。
 * 发送请求并等待响应：通过Netty发送RPC协议，并异步等待响应。
 * 处理响应：返回响应数据或抛出异常
 */
public class CgLibProxy implements MethodInterceptor{

    private final String serviceName;
    private final String version;
    private final FaultTolerant faultTolerant;

    private final long time;
    private final TimeUnit timeUnit;
    private final LoadBalance loadBalance;
    public CgLibProxy(Class claz, RpcReference rpcReference) {
        this.serviceName = claz.getName();
        this.version = rpcReference.version();
        this.faultTolerant = rpcReference.faultTolerant();
        this.time = rpcReference.time();
        this.timeUnit = rpcReference.timeUnit();
        this.loadBalance = rpcReference.loadBalance();
    }


    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        final RpcProtocol rpcProtocol = new RpcProtocol();
        // 构建消息头
        MsgHeader header = new MsgHeader();
        long requestId = RpcRequestHolder.getRequestId();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        header.setMsgType((byte) MsgType.REQUEST.ordinal());
        header.setStatus((byte) 0x1);
        rpcProtocol.setHeader(header);

//
//        final byte[] serialization = RpcSerialization.JSON.name.getBytes();
//        header.setSerializationLen(serialization.length);
//        header.setSerialization(serialization);



        final RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodCode(method.hashCode());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setServiceVersion(version);
        if (null!=objects && objects.length >0){
          Object object = objects[0];
          if(object != null){
              rpcRequest.setParameterTypes(object.getClass());
              rpcRequest.setParameter(object);
          }
        }

        rpcProtocol.setBody(rpcRequest);

        final List<URL> urls = RegistryFactory.get(Properties.getRegister().getName()).discoveries(serviceName,version);
        if(urls.isEmpty()){
            throw new Exception("无服务可用： "+serviceName);
        }
        final LoadBalancer loadBalancer = LoadBalancerFactory.get(loadBalance);
        final URL url = loadBalancer.select(urls);

        final ChannelFuture channelFuture = Cache.CHANNEL_FUTURE_MAP.get(new Host(url.getIp(),url.getPort()));
        final List<Filter> clientBeforeFilters = FilterFactory.getClientBeforeFilters();
        if(!clientBeforeFilters.isEmpty()){
            final FilterData<RpcRequest> rpcRequestFilterData = new FilterData<>(rpcRequest);
            final FilterLoader filterLoader = new FilterLoader();
            filterLoader.addFilter(clientBeforeFilters);
            final FilterResponse filterResponse = filterLoader.doFilter(rpcRequestFilterData);
            if(!filterResponse.getResult()){
                throw filterResponse.getException();
            }
        }

        // 发送
        channelFuture.channel().writeAndFlush(rpcProtocol);
        RpcFuture<RpcResponse> future = new RpcFuture(new DefaultPromise(new DefaultEventLoop()), time);
        RpcRequestHolder.REQUEST_MAP.put(requestId, future);
        RpcResponse rpcResponse = future.getPromise().sync().get(future.getTimeout(), timeUnit);

        final List<Filter> clientAfterFilters = FilterFactory.getClientAfterFilters();
        if (!clientBeforeFilters.isEmpty()){
            final FilterData<RpcResponse> rpcResponseFilterData = new FilterData<>(rpcResponse);
            final FilterLoader filterLoader = new FilterLoader();
            filterLoader.addFilter(clientAfterFilters);
            final FilterResponse filterResponse = filterLoader.doFilter(rpcResponseFilterData);
            if (!filterResponse.getResult()) {
                throw filterResponse.getException();
            }
        }
        // 发生异常
        if (rpcResponse.getException()!=null){
            rpcResponse.getException().printStackTrace();
            final FaultContext faultContext = new FaultContext(url,urls,rpcProtocol,requestId,rpcResponse.getException());
            final FaultTolerantStrategy faultTolerantStrategy = FaultTolerantFactory.get(faultTolerant);
            return faultTolerantStrategy.handler(faultContext);
        }
        return rpcResponse.getData();
    }
}
