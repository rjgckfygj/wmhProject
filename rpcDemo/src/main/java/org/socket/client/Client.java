package org.socket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.proxy.IProxy;
import org.proxy.ProxyFactory;
import org.common.Cache;
import org.common.Host;
import org.common.URL;
import org.common.constants.Register;
import org.event.RpcListerLoader;
import org.filter.FilterFactory;
import org.register.RegistryFactory;
import org.register.RegistryService;
import org.router.LoadBalancerFactory;
import org.socket.codec.RpcDecoder;
import org.socket.codec.RpcEncoder;
import org.tolerant.FaultTolerantFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Client {



    //Bootstrap 是 Netty 提供的客户端启动类，用于配置客户端的连接和行为。
    private  Bootstrap bootstrap; //Netty 的 Bootstrap 对象，用于配置客户端。
    private  EventLoopGroup eventLoopGroup; //Netty 的事件循环组，用于管理客户端的 I/O 事件



    /***
     * 一个基于Netty框架的客户端初始化逻辑
     * 用于创建一个能够发送和接收RPC（远程过程调用）消息的客户端
     */
    public void run(){

        bootstrap = new Bootstrap();   //Bootstrap：Netty中用于客户端的启动类，用于配置客户端的连接和事件处理。
        eventLoopGroup = new NioEventLoopGroup(4);  //NioEventLoopGroup：Netty的事件循环组，用于处理I/O事件。这里创建了一个包含4个线程的事件循环组
        bootstrap.group(eventLoopGroup) //指定事件循环组
                .channel(NioSocketChannel.class)  //使用 NioSocketChannel 作为客户端通道。
                .option(ChannelOption.SO_KEEPALIVE,true) //设置 SO_KEEPALIVE 选项，保持连接活跃
                .handler(new ChannelInitializer<SocketChannel>() {

                    //添加自定义的编解码器（RpcEncoder 和 RpcDecoder）到 ChannelPipeline。
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS))
                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new ClientHandler());  //向管道中添加了三个处理器
                    }
                });

        //将初始化好的Bootstrap对象存储到Cache中，供后续使用
        Cache.BOOT_STRAP = bootstrap;
    }

    /***
     * 从服务注册中心获取服务的地址列表，并通过Netty框架与这些服务端建立连接
     * 1.服务发现
     * 2.建立连接
     * 3.缓存管理
     * @throws Exception
     */
    public void connectServer() throws Exception{
        for(URL url: Cache.SUBSCRIBE_SERVICE_LIST){
            final RegistryService registryService = RegistryFactory.get(Register.ZOOKEEPER);
            final List<URL> urls = registryService.discoveries(url.getServiceName(),url.getVersion());
            if(!urls.isEmpty()){
                for(URL u:urls){
                    final ChannelFuture connect = bootstrap.connect(u.getIp(),u.getPort());
                    Cache.CHANNEL_FUTURE_MAP.put(new Host(u.getIp(),u.getPort()),connect);
                }
            }

        }
    }

    public void init() throws IOException,ClassNotFoundException{
        new RpcListerLoader().init();
        FaultTolerantFactory.init();
        RegistryFactory.init();
        FilterFactory.initClient();
        ProxyFactory.init();
        LoadBalancerFactory.init();
//        SerializationFactory.init();
    }

//    /**
//     * 服务注册：将服务名称和服务地址（URL）关联起来，并建立与服务端的连接。
//     * @param serviceName
//     */
//    public void registerBean(String serviceName){
//        final URL url = new URL(this.host,port);
//
//        //使用 Cache.services（一个静态缓存）将服务名称（serviceName）和服务地址（url）关联起来。
//        Cache.services.put(new ServiceName(serviceName),url);
//
//        //使用 Netty 的 bootstrap（客户端启动器）连接到服务端的 host 和 port。
//        //channelFuture 是一个异步操作的未来对象，表示连接操作的结果。
//        channelFuture = bootstrap.connect(host,port);
//
//        //将服务地址（url）和连接对象（channelFuture）存入另一个缓存 Cache.channelFutureMap，以便后续使用。
//        Cache.channelFutureMap.put(url,channelFuture);
//    }

//    public static void main(String[] args) throws Exception{
//
//
//        final Client client = new Client("127.0.0.1",8081);
//        client.run();
//        client.init();
//
//        // 从注册中心订阅服务
//        final RegistryService registryService = RegistryFactory.get(Register.ZOOKEEPER);
//        final URL url = new URL();
//        url.setServiceName(IHelloService.class.getName());
//        url.setVersion("1.0");
//        registryService.subscribe(url);
//
//        client.connectServer();
//
//        //创建服务代理
//        final IProxy iProxy = ProxyFactory.get(RpcProxy.CG_LIB);    //从代理工厂获取一个代理生成器实例，这里使用CGLIB作为代理工具。
//        final IHelloService proxy = iProxy.getProxy(IHelloService.class);   //通过代理生成器创建一个HelloService接口的代理实例。
//
//
//        System.out.println(proxy.hello("hello ~"));
//        System.out.println("=====");
//        System.out.println(proxy.hello("hello ~"));
//
//
//        /**
//         * 客户端启动流程：注册服务、创建代理对象并调用远程方法
//         */
//
////        final Client client = new Client("127.0.0.1",8081);
////        //调用 registerBean 方法，将 HelloService 的类名注册到客户端缓存中，并建立与服务端的连接。
////        client.registerBean(HelloService.class.getName());
////
////        //通过 ProxyFactory 获取一个代理工厂实例，指定使用 CGLIB 代理（RpcProxy.CG_LIB）
////        final IProxy iProxy = ProxyFactory.get(RpcProxy.CG_LIB);
////
////        //使用代理工厂创建 HelloService 的代理对象。这个代理对象的方法调用将被转发到远程服务端。
////        final HelloService proxy = iProxy.getProxy(HelloService.class);
////
////        //通过代理对象调用 hello 方法，并打印返回结果。
////        System.out.println(proxy.hello("你好"));
//
//        /**
//         * RPC客户端请求流程：
//         * 定义目标服务和方法：通过反射获取目标服务类和方法。
//         * 构建RPC请求：设置服务类名、方法名、参数等信息。
//         * 发送请求：通过Netty客户端发送请求。
//         * 异步处理响应：创建RpcFuture对象，注册请求ID，并等待响应。
//         * 打印结果：输出RPC响应的内容。
//         */
////        //创建客户端实例，连接到本地服务器（127.0.0.1:8081
////        final Client nettyClient = new Client("127.0.0.1",8081);
////        //构建 RpcProtocol 消息：
////        final RpcProtocol rpcProtocol = new RpcProtocol();
////
////        //构建消息头
////        MsgHeader header = new MsgHeader();
////        long requestId = 123;
////        header.setMagic(ProtocolConstants.MAGIC);
////        header.setVersion(ProtocolConstants.VERSION);
////        header.setRequestId(requestId);
////
////        final byte[] serialization = RpcSerialization.JSON.name.getBytes();
////        header.setSerializationLen(serialization.length);
////        header.setSerialization(serialization);
////        header.setMsgType((byte) MsgType.REQUEST.ordinal());
////        header.setStatus((byte) 0x1);
////
////        rpcProtocol.setHeader(header);
////        final RpcRequest rpcRequest = new RpcRequest();
////
////        final Class<HelloService> aClass = HelloService.class; //获取HelloService类的Class对象，用于后续的反射操作。
////        rpcRequest.setClassName(aClass.getName()); //设置RPC请求的目标服务类名（HelloService）。
////        final Method method = aClass.getMethod("hello",String.class); //通过反射获取HelloService类中名为hello的方法，该方法接收一个String类型的参数。
////        rpcRequest.setMethodCode(method.hashCode()); //设置方法的哈希码（hashCode）作为方法的唯一标识。这通常用于服务端快速定位方法
////        rpcRequest.setMethodName(method.getName());
////        rpcRequest.setServiceVersion("1.0");
////        rpcRequest.setParameterTypes(method.getParameterTypes()[0]);
////        rpcRequest.setParameter("wmh"); //设置方法调用的参数值为"wmh"
////        rpcProtocol.setBody(rpcRequest);
////
////        nettyClient.sendRequest(rpcProtocol);
////
////        //创建一个RpcFuture对象，用于异步处理RPC响应。
////        //new DefaultPromise<>(new DefaultEventLoop())：创建一个DefaultPromise对象，用于管理异步操作的结果。
////        //3000：设置超时时间为3000毫秒（3秒）。
////        RpcFuture<RpcResponse> future = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()),3000);
////
////        //将请求ID（requestId）与future对象存储到一个全局的请求映射表（REQUEST_MAP）中。
////        // 这通常用于在收到响应时，根据请求ID找到对应的future对象。
////        RpcRequestHolder.REQUEST_MAP.put(requestId,future);
////        //调用future.getPromise().sync()，使当前线程同步等待响应。
////        Object rpcResponse = future.getPromise().sync().get(future.getTimeout(), TimeUnit.MILLISECONDS);
////        System.out.println(rpcResponse);
//
//    }
}
