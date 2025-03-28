package org.filter.client;

import org.filter.Filter;
import org.socket.codec.RpcRequest;

public interface ClientBeforeFilter extends Filter<RpcRequest> {
}
