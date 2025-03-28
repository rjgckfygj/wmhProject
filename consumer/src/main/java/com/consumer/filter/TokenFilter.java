package com.consumer.filter;

import org.filter.FilterData;
import org.filter.FilterResponse;
import org.filter.client.ClientBeforeFilter;
import org.socket.codec.RpcRequest;

public class TokenFilter implements ClientBeforeFilter {
    @Override
    public FilterResponse doFilter(FilterData<RpcRequest> filterData) {
        final RpcRequest rpcRequest = filterData.getObject();
        rpcRequest.getClientAttachments().put("token","wmh");
        return new FilterResponse(true,null);
    }
}
