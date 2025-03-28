package org.config;

import org.annotation.RpcReference;
import org.common.URL;
import org.common.constants.FaultTolerant;
import org.common.constants.Register;
import org.common.constants.RpcProxy;
import org.event.RpcListerLoader;
import org.filter.FilterFactory;
import org.proxy.IProxy;
import org.proxy.ProxyFactory;
import org.register.RegistryFactory;
import org.register.RegistryService;
import org.router.LoadBalancerFactory;
import org.socket.client.Client;
import org.socket.serialization.SerializationFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.tolerant.FaultTolerantFactory;

import java.lang.reflect.Field;


public class ConsumerPostProcessor implements BeanPostProcessor, InitializingBean {

    RpcProperties rpcProperties;

    public ConsumerPostProcessor(RpcProperties rpcProperties){
        this.rpcProperties = rpcProperties;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        new RpcListerLoader().init();
        FaultTolerantFactory.init();
        RegistryFactory.init();
        FilterFactory.initClient();
        ProxyFactory.init();
        LoadBalancerFactory.init();
        SerializationFactory.init();
        final Client client= new Client();
        client.run();
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        //获取所有字段
        final Field[] fields = bean.getClass().getDeclaredFields();

        //遍历所有字段找到RpcReference 注解的字段
        for(Field field: fields){
            if(field.isAnnotationPresent(RpcReference.class)){
                final RegistryService registryService = RegistryFactory.get(Register.ZOOKEEPER);
                final Class<?> aClass = field.getType();
                final RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                field.setAccessible(true);
                Object object = null;
                try{
                    final IProxy iproxy = ProxyFactory.get(RpcProxy.CG_LIB);
                    final Object proxy = iproxy.getProxy(aClass,rpcReference);
                    //创建代理对象
                    object = proxy;
                    final URL url = new URL();
                    url.setServiceName(aClass.getName());
                    url.setVersion(rpcReference.version());
                    registryService.subscribe(url);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try{
                    field.set(bean,object);
                    field.setAccessible(false);
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
