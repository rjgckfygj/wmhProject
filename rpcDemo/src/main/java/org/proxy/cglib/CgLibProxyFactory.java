package org.proxy.cglib;

import net.sf.cglib.proxy.Enhancer;
import org.annotation.RpcReference;
import org.proxy.IProxy;

/**
 * 代理工厂类
 * 通过 CGLIB 的 Enhancer 创建目标类的代理对象，并将方法调用转发到 CgLibProxy 的 intercept 方法中。
 * 这个类的作用是封装代理对象的创建过程，使得用户可以通过简单的调用获取代理对象。
 * @param <T>
 */
public class CgLibProxyFactory<T> implements IProxy {

    @Override
    public <T> T getProxy(Class<T> claz, RpcReference rpcReference) throws InstantiationException, IllegalAccessException {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(claz);
        enhancer.setCallback(new CgLibProxy(claz,rpcReference));
        return (T) enhancer.create();
    }
}
