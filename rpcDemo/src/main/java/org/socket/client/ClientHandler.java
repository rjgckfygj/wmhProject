package org.socket.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.common.RpcFuture;
import org.common.RpcRequestHolder;
import org.common.constants.MsgType;
import org.socket.codec.MsgHeader;
import org.socket.codec.RpcProtocol;
import org.socket.codec.RpcRequest;
import org.socket.codec.RpcResponse;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> rpcResponseRpcProtocol) throws Exception {
        long requestId = rpcResponseRpcProtocol.getHeader().getRequestId();
        RpcFuture<RpcResponse> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);
        if (future != null) {
            future.getPromise().setSuccess(rpcResponseRpcProtocol.getBody());
        } else {
            log.warn("No future found for request ID: {}", requestId);
        }
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext channelHandlerContext,Object object) throws Exception{
        if(object instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) object).state();
            if(state == IdleState.WRITER_IDLE){
                log.info("write idle happen [{}]",channelHandlerContext.channel().remoteAddress());
                System.out.println("write idle happen [{}] "+channelHandlerContext.channel().remoteAddress());
                Channel channel = channelHandlerContext.channel();

                // 创建心跳消息
                RpcProtocol<RpcRequest> heartbeatMessage = new RpcProtocol<>();
                heartbeatMessage.setHeader(new MsgHeader());
                heartbeatMessage.getHeader().setMsgType( (byte) MsgType.HEARTBEAT.ordinal());
                heartbeatMessage.setBody(null); // 心跳消息通常不需要携带具体数据

                // 发送心跳消息
                channel.writeAndFlush(heartbeatMessage).addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error("Failed to send heartbeat to [{}]", channel.remoteAddress(), future.cause());
                    }
                });
            }
        } else {
            super.userEventTriggered(channelHandlerContext, object);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
