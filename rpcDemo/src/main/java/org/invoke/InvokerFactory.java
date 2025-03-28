package org.invoke;

import org.common.constants.RpcInvoker;
import org.spi.ExtensionLoader;

import java.io.IOException;

/**
 * 使用InvokerFactory工厂类创建Invoker实例。
 */
public class InvokerFactory {
//    public static Map<RpcInvoker,Invoker> invokerInvokerMap = new HashMap<>();
//
//    static {
//        invokerInvokerMap.put(RpcInvoker.JDK,new JdkReflectionInvoker());
//    }

    public static Invoker get(RpcInvoker rpcInvoker){
        return ExtensionLoader.getInstance().get(rpcInvoker.name);
    }

    public static Invoker get(String name){
        return ExtensionLoader.getInstance().get(name);
    }

    public static void init() throws IOException,ClassNotFoundException{
        ExtensionLoader.getInstance().loadExtension(Invoker.class);
    }

}
