package org.register;

import org.common.Cache;
import org.common.ServiceName;
import org.common.URL;

import java.util.ArrayList;
import java.util.List;


/**
 * 定义了一个抽象类 AbstractZookeeperRegistry，它实现了 RegistryService 接口。
 * 它的主要功能是管理服务的注册、注销和发现，使用了一个名为 Cache 的类来存储服务的 URL 信息。
 */

public abstract class AbstractZookeeperRegistry implements RegistryService{

    //将一个服务的 URL 注册到缓存中
    @Override
    public void register(URL url) throws Exception{
        final ServiceName serviceName = new ServiceName(url.getServiceName(),url.getVersion());
        if(!Cache.SERVICE_URLS.containsKey(serviceName)){
            Cache.SERVICE_URLS.put(serviceName,new ArrayList<>());
        }
        Cache.SERVICE_URLS.get(serviceName).add(url);

    }

    //从缓存中注销一个服务的 URL
    @Override
    public void unRegister(URL url) throws Exception {
        final ServiceName serviceName = new ServiceName(url.getServiceName(),url.getVersion());
        if(Cache.SERVICE_URLS.containsKey(serviceName)){
            Cache.SERVICE_URLS.get(serviceName).remove(url);
        }
    }

    //根据服务名称和版本号，从缓存中获取所有注册的 URL。
    @Override
    public List<URL> discoveries(String serviceName, String version) throws Exception {
        final ServiceName key = new ServiceName(serviceName,version);
        List<URL> urls = Cache.SERVICE_URLS.get(key);
        return urls;
    }
}
