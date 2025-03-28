package org.socket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.annotation.RpcService;
import org.common.URL;
import org.common.constants.Register;
import org.config.Properties;
import org.filter.FilterFactory;
import org.invoke.InvokerFactory;
import org.register.RegistryFactory;
import org.socket.codec.RpcDecoder;
import org.socket.codec.RpcEncoder;
import org.socket.serialization.SerializationFactory;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;


/**
 * @description:
 * @gitee: https://gitee.com/XhyQAQ
 * @copyright: B站: https://space.bilibili.com/152686439
 * @Author: Xhy
 * @CreateTime: 2024-05-07 12:09
 */
public class Server {
    private final Integer port;
    private ServerBootstrap bootstrap;

    public Server(Integer port){
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            bootstrap= new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RpcEncoder());
                            ch.pipeline().addLast(new RpcDecoder());
                            ch.pipeline().addLast(new ServerHandler());
                            ch.pipeline().addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)   //设置服务器的连接队列大小为128。
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            if(port == null){
                bootstrap.bind(0).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if(channelFuture.isSuccess()){
                            Channel channel = channelFuture.channel();
                            InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
                            Properties.setPort(localAddress.getPort());
                        }
                    }
                }).sync().channel().closeFuture().sync();
            }else {
                bootstrap.bind(port).sync().channel().closeFuture().sync();
            }

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

//    public void registerBean(Class clazz) throws Exception{
//        final URL url = new URL(host,port);
//
//        //检查服务类是否包含@RpcService注解
//        if(!clazz.isAnnotationPresent(RpcService.class)){
//            throw new Exception(clazz.getName() + "没有注解 RpcService");
//        }
//
//        //获取注解信息
//        final RpcService rpcService = (RpcService) clazz.getAnnotation(RpcService.class);
//        String serviceName = clazz.getInterfaces()[0].getName();
//        if(!(rpcService.serviceInterface().equals(void.class))){
//            serviceName = rpcService.serviceInterface().getName();
//        }
//
//        url.setServiceName(serviceName);
//        url.setVersion(rpcService.version());
//
//        //注册服务到Zookeeper
//        final RegistryService registryService = RegistryFactory.get(Register.ZOOKEEPER);
//        registryService.register(url);
//
//        final String key = ServiceNameBuilder.builderServiceKey(serviceName,rpcService.version());
//        Cache.SERVICE_MAP.put(key,clazz.newInstance());
//    }

    public void init() throws IOException,ClassNotFoundException{
        RegistryFactory.init();
        FilterFactory.initServer();
        InvokerFactory.init();
        SerializationFactory.init();
    }

//    public static void main(String[] args) throws Exception {
//
//        final Server server = new Server(8081);
//        server.init();
//
////        FilterFactory.registerServerBeforeFilter(new ServerTokenFilter());
////        FilterFactory.registerServerAfterFilter(new Filter() {
////            @Override
////            public FilterResponse doFilter(FilterData filterData) {
//////                final RpcRequest object = (RpcRequest) filterData.getObject();
////                System.out.println("服务端后置拦截器");
////                return new FilterResponse(true,null);
////            }
////        });
//        server.registerBean(HelloService.class);
//        server.run();
//    }
}
