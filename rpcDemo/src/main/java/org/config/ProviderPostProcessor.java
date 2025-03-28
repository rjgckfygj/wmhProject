package org.config;

import org.annotation.RpcService;
import org.common.Cache;
import org.common.URL;
import org.filter.FilterFactory;
import org.invoke.InvokerFactory;
import org.register.RegistryFactory;
import org.register.RegistryService;
import org.socket.serialization.SerializationFactory;
import org.socket.server.Server;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.utils.IpUtil;
import org.utils.ServiceNameBuilder;

public class ProviderPostProcessor implements InitializingBean, BeanPostProcessor {

    private RpcProperties rpcProperties;

    private final String ip = IpUtil.getIP();

    public ProviderPostProcessor(RpcProperties rpcProperties){
        this.rpcProperties = rpcProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        RegistryFactory.init();
        FilterFactory.initServer();
        InvokerFactory.init();
        SerializationFactory.init();
        Thread t = new Thread(() -> {
            final Server server = new Server(rpcProperties.getPort());
            try{
                server.run();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        //找到bean上带有RpcService注解的类
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
//        System.out.println("org.config ProviderPostProcessor.java  rpcService = "+rpcService.serviceInterface());
        if(rpcService != null){
            String serviceName = beanClass.getInterfaces()[0].getName();
            if(!rpcService.serviceInterface().equals(void.class)){
                serviceName = rpcService.serviceInterface().getName();
            }
            try{
                RegistryService registryService = RegistryFactory.get(rpcProperties.getRegistry().getName());
                final URL url = new URL();
                url.setPort(Properties.getPort());
                url.setIp(ip);
                url.setServiceName(serviceName);
                url.setVersion(rpcService.version());
                registryService.register(url);

                //缓存
                final String key = ServiceNameBuilder.builderServiceKey(serviceName,rpcService.version());
                Cache.SERVICE_MAP.put(key,bean);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return  bean;
    }
}
