package org.socket.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.common.constants.MsgType;
import org.common.constants.ProtocolConstants;
import org.config.Properties;
import org.socket.serialization.RpcSerialization;
import org.socket.serialization.SerializationFactory;

import java.util.List;

/**
 * @description:  Netty解码器
 * 将从网络接收到的字节流（ByteBuf）解码为 RpcProtocol 对象，以便进一步处理
 *
 * 1.检查协议头是否完整
 * 2，读取协议头字段
 * 3.读取序列化算法
 * 4.读取消息体
 * 5.反序列化消息体
 * 6.将解码后的头和体封装为RpcProtocol对象
 */
public class RpcDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {

        //若可读字节数少于协议头长度，说明还没有接收完整个协议头，直接返回
        if (in.readableBytes() < ProtocolConstants.HEADER_TOTAL_LEN){
            return ;
        }
        in.markReaderIndex(); // 标记当前读取位置

        short magic = in.readShort(); // 读取魔数
        if(magic != ProtocolConstants.MAGIC){
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        // 读取版本字段
        byte version = in.readByte();
        // 读取消息类型
        byte msgType = in.readByte();
        // 读取响应状态
        byte status = in.readByte();
        // 读取请求 ID
        long requestId = in.readLong();
        // 获取序列化算法长度
        final int dataLength = in.readInt();

        //如果可读字节数少于序列化算法的长度，说明还没有接收完整个序列化算法字段，回退并返回
        if(in.readableBytes() < dataLength){
            in.resetReaderIndex(); // 回退到标记位置
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);  // 读取序列化算法的字节



        //处理消息的类型
        MsgType msgTypeEnum = MsgType.findByType(msgType);
        if(msgTypeEnum == null){
            return;
        }

        //构建消息头
        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
//        header.setSerialization(data);
//        header.setSerializationLen(dataLength);
        header.setMsgLen(dataLength);

        RpcSerialization rpcSerialization = SerializationFactory.get(Properties.getSerialization());
        RpcProtocol protocol = new RpcProtocol<>();
        protocol.setHeader(header);

        switch (msgTypeEnum){
            //请求消息
            case REQUEST:
                RpcRequest request = rpcSerialization.deserialize(data,RpcRequest.class);
                protocol.setBody(request);
                break;

            //响应消息
            case RESPONSE:
                RpcResponse response = rpcSerialization.deserialize(data,RpcResponse.class);
                protocol.setBody(response);
                break;
        }

        out.add(protocol);

    }
}
