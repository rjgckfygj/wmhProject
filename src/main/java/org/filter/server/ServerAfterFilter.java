package org.filter.server;

import org.filter.Filter;
import org.socket.codec.RpcResponse;

public interface ServerAfterFilter extends Filter<RpcResponse> {
}

