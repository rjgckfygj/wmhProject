package org.event;

import io.netty.channel.ChannelFuture;
import org.common.Cache;
import org.common.Host;
import org.common.ServiceName;
import org.common.URL;

import java.util.ArrayList;

/**
 * 监听 RPC（远程过程调用）事件，并在事件触发时执行一些操作，包括服务注册和网络连接的建立
 */
public class AddRpcListener implements IRpcLister<AddRpcEventData>{
    @Override
    public void exec(AddRpcEventData addRpcLister){
        final URL url = (URL) addRpcLister.getData();
        System.out.println("url = "+url.toString());
        final ServiceName serviceName = new ServiceName(url.getServiceName(),url.getVersion());
        if(!Cache.SERVICE_URLS.containsKey(serviceName)){
            Cache.SERVICE_URLS.put(serviceName,new ArrayList<>());
        }
        Cache.SERVICE_URLS.get(serviceName).add(url);

        //根据服务的 IP 和端口建立网络连接，并将连接信息存储到缓存中
        final Host ip = new Host(url.getIp(), url.getPort());
        if(!Cache.CHANNEL_FUTURE_MAP.containsKey(ip)){
            ChannelFuture channelFuture = Cache.BOOT_STRAP.connect(url.getIp(), url.getPort());
            Cache.CHANNEL_FUTURE_MAP.put(ip,channelFuture);
        }
    }
}
