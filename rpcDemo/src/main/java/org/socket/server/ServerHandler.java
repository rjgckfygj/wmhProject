package org.socket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.common.constants.MsgType;
import org.common.constants.RpcInvoker;
import org.config.Properties;
import org.filter.*;
import org.invoke.Invocation;
import org.invoke.InvokerFactory;
import org.invoke.Invoker;
import org.socket.codec.MsgHeader;
import org.socket.codec.RpcProtocol;
import org.socket.codec.RpcRequest;
import org.socket.codec.RpcResponse;

import java.util.List;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcRequest> rpcRequestRpcProtocol) throws Exception {

        final RpcRequest rpcRequest = rpcRequestRpcProtocol.getBody();
        final MsgHeader header = rpcRequestRpcProtocol.getHeader();

        // 检查是否为心跳请求
        if (header.getMsgType() == MsgType.HEARTBEAT.ordinal()) {
            // 如果是心跳请求，记录日志并直接返回，不进行后续处理
            log.info("Heartbeat request received from [{}], ignoring...", channelHandlerContext.channel().remoteAddress());
            return;
        }

        final RpcResponse response = new RpcResponse();
        final RpcProtocol<RpcResponse> resRpcProtocol = new RpcProtocol();
        header.setMsgType((byte) MsgType.RESPONSE.ordinal());
        resRpcProtocol.setHeader(header);
        final Invoker invoker = InvokerFactory.get(Properties.getInvoke());


        try {

            final List<Filter> serverBeforeFilters = FilterFactory.getServerBeforeFilters();
            if (!serverBeforeFilters.isEmpty()){
                final FilterData<RpcRequest> rpcRequestFilterData = new FilterData<>(rpcRequest);
                final FilterLoader filterLoader = new FilterLoader();
                filterLoader.addFilter(serverBeforeFilters);
                final FilterResponse filterResponse = filterLoader.doFilter(rpcRequestFilterData);
                if (!filterResponse.getResult()) {
                    throw filterResponse.getException();
                }
            }

            // 执行
            final Object data = invoker.invoke(new Invocation(rpcRequest));
            response.setData(data);
        }catch (Exception e){
            response.setException(e);
        }finally {
            // 执行服务器端后置过滤器
            final List<Filter> serverAfterFilters = FilterFactory.getServerAfterFilters();
            if (!serverAfterFilters.isEmpty()){
                final FilterData<RpcResponse> rpcResponseFilterData = new FilterData<>(response);
                final FilterLoader filterLoader = new FilterLoader();
                filterLoader.addFilter(serverAfterFilters);
                final FilterResponse filterResponse = filterLoader.doFilter(rpcResponseFilterData);
                if (!filterResponse.getResult()) {
                    throw filterResponse.getException();
                }
            }
        }
        resRpcProtocol.setBody(response);
        channelHandlerContext.writeAndFlush(resRpcProtocol);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                // 如果超过30秒没有读请求，关闭连接
                log.info("No read request for 30 seconds, closing connection with [{}]", ctx.channel().remoteAddress());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception caught in ServerHandler", cause);
        ctx.close();
    }
}
