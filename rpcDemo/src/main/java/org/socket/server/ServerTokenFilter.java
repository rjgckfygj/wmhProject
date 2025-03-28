package org.socket.server;

import org.filter.FilterData;
import org.filter.FilterResponse;
import org.filter.server.ServerBeforeFilter;
import org.socket.codec.RpcRequest;


/**
 * 在服务端接收 RPC 请求之前，对请求中的 Token 进行验证。
 * 如果 Token 不符合预期，则拒绝请求并返回错误信息。
 */
public class ServerTokenFilter implements ServerBeforeFilter {
    @Override
    public FilterResponse doFilter(FilterData<RpcRequest> filterData) {
        final RpcRequest rpcRequest= filterData.getObject();
        Object value = rpcRequest.getClientAttachments().get("token");
        if(!value.equals("wmh")){
            return new FilterResponse(false,new Exception("token 不正确,当前token为:" + value));

        }
        return new FilterResponse(true,null);
    }
}
