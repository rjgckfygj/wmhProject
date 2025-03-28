package org.provider.filter;
import org.filter.FilterData;
import org.filter.FilterResponse;
import org.filter.server.ServerBeforeFilter;
import org.socket.codec.RpcRequest;

public class TokenFilter implements ServerBeforeFilter{
    @Override
    public FilterResponse doFilter(FilterData<RpcRequest> filterData) {
        final RpcRequest rpcRequest = filterData.getObject();
        Object token = rpcRequest.getClientAttachments().get("token");
        if(!token.equals("wmh")){
            return new FilterResponse(false,new Exception("token不正确"));
        }
        return new FilterResponse(true,null);
    }
}
