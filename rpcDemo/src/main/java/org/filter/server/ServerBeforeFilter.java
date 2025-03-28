package org.filter.server;

import org.filter.Filter;
import org.socket.codec.RpcRequest;

public interface ServerBeforeFilter extends Filter<RpcRequest> {
}
