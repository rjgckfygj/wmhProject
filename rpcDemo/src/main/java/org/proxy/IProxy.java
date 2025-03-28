package org.proxy;

import org.annotation.RpcReference;

public interface IProxy {
    <T> T getProxy(Class<T> claz, RpcReference rpcReference) throws InstantiationException,IllegalAccessException;
}
