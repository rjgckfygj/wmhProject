package org.proxy;

import org.common.constants.RpcProxy;
import org.spi.ExtensionLoader;

import java.io.IOException;

/**
 * 工厂类，用于根据不同的代理类型（通过 RpcProxy 枚举指定）来获取对应的代理实现（IProxy 接口的实现）
 */
public class ProxyFactory {


    public static IProxy get(RpcProxy rpcProxy){

        return ExtensionLoader.getInstance().get(rpcProxy.name);
    }

    public static IProxy get(String name){
        return ExtensionLoader.getInstance().get(name);
    }

    public static void init() throws IOException,ClassNotFoundException{
        ExtensionLoader.getInstance().loadExtension(IProxy.class);
    }
}
