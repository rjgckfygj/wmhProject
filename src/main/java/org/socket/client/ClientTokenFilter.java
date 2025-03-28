package org.socket.client;

import org.filter.FilterData;
import org.filter.FilterResponse;
import org.filter.client.ClientBeforeFilter;
import org.socket.codec.RpcRequest;

public class ClientTokenFilter implements ClientBeforeFilter {
    @Override
    public FilterResponse doFilter(FilterData<RpcRequest> filterData) {

        //通过 filterData.getObject() 获取封装在 FilterData 中的 RpcRequest 对象
        final RpcRequest rpcRequest = filterData.getObject();

        rpcRequest.getClientAttachments().put("token","wmh123");
        return new FilterResponse(true,null);
    }
}
