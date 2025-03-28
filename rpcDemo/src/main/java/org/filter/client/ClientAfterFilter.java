package org.filter.client;

import org.filter.Filter;
import org.socket.codec.RpcResponse;

public interface ClientAfterFilter extends Filter<RpcResponse> {
}

