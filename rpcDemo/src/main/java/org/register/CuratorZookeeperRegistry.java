package org.register;

import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.common.Cache;
import org.common.URL;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.config.Properties;
import org.event.*;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.*;

public class CuratorZookeeperRegistry extends AbstractZookeeperRegistry{

    //连接失败等待重试时间
    public static final int BASE_SLEEP_TIME_MS = 1000;
    //重试次数
    public static final int MAX_RETRIES = 3;
    // ZooKeeper 中的根路径
    public static final String ROOT_PATH ="/wmh_rpc";
    // 提供者路径前缀
    public static final String PROVIDER = "/provider";
    // Curator 客户端实例
    private final CuratorFramework client;

    /**
     *初始化 Curator 客户端并连接到 ZooKeeper 服务器
     */
    public CuratorZookeeperRegistry(){
        client = CuratorFrameworkFactory.newClient(Properties.getRegister().getHost(), new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        client.start();
        JsonInstanceSerializer<URL> serializer = new JsonInstanceSerializer(URL.class);
    }

    //将服务的 URL 注册到 ZooKeeper 中

    /**
     *功能：将服务的 URL 注册到 ZooKeeper 中。
     * 1.检查根路径是否存在，如果不存在则创建（持久节点）。
     * 2.计算服务的完整路径（providerDataPath）。
     * 3如果路径已存在，则删除旧节点。
     * 4.创建一个临时节点（EPHEMERAL），并将服务的 URL 序列化为 JSON 字节数组存储在节点中。
     * ROOT_PATH : 根路径
     * ROOT_PATH + ServiceName + $ version : 监听
     * ROOT_PATH + ServiceName + $ version + Host + Port : 节点
     * @param url
     * @throws Exception
     */
    public void register(URL url) throws Exception{
        if (!existNode(ROOT_PATH)) {
            client.create().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT).forPath(ROOT_PATH, "".getBytes());;
        }

        final String providerDataPath = getProviderDataPath(url);

        if (existNode(providerDataPath)) {
            deleteNode(providerDataPath);
        }
        client.create().creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL).forPath(providerDataPath, JSON.toJSONString(url).getBytes());

        // 检查节点是否成功创建
        if (!existNode(providerDataPath)) {
            throw new Exception("注册失败：节点未创建成功，路径为 " + providerDataPath);
        }

        // 检查节点数据是否正确
        byte[] data = client.getData().forPath(providerDataPath);
        String storedData = new String(data, StandardCharsets.UTF_8);
        if (!storedData.equals(JSON.toJSONString(url))) {
            throw new Exception("注册失败：节点数据不一致，路径为 " + providerDataPath);
        }

        System.out.println("注册成功：路径为 " + providerDataPath);

    }

    /***
     * 从 ZooKeeper 中注销服务
     * 1.删除服务对应的 ZooKeeper 节点。
     * 2.调用父类的 unRegister 方法，从缓存中移除服务的 URL。
     * @param url
     * @throws Exception
     */
    public void unRegister(URL url) throws Exception{
        deleteNode(getProviderDataPath(url));
        super.unRegister(url);
    }

    /***
     * 从 ZooKeeper 中发现服务的 URL。
     * 1.尝试从缓存中获取服务的URL列表
     * 2.如果缓存中没有，从Zookeeper中获取子节点列表
     * 3.解析子节点路径，构造URL对象并返回
     * @param serviceName
     * @param version
     * @return
     * @throws Exception
     */
    public List<URL> discoveries(String serviceName,String version) throws Exception{
        List<URL> urls = super.discoveries(serviceName,version);
        if(null == urls || urls.isEmpty()){
            final List<String> strings = client.getChildren().forPath(getProviderPath(serviceName,version));
            if(!strings.isEmpty()){
                urls = new ArrayList<>();
                for(String string : strings){
                    final String[] split = string.split(":");
                    urls.add(new URL(split[0],Integer.parseInt(split[1])));
                }
            }
        }
        return urls;
    }

    /***
     * 订阅服务变更事件，并监听 ZooKeeper 节点的变化。
     * 1.将服务添加到订阅列表
     * 2.调用watchNodeDataChange方法监听节点的变化
     * @param url
     * @throws Exception
     */
    @Override
    public void subscribe(URL url) throws Exception {
        final String path = getProviderPath(url.getServiceName(),url.getVersion());
        Cache.SUBSCRIBE_SERVICE_LIST.add(url);
        this.watchNodeDataChange(path);
    }

    @Override
    public void unSubscribe(URL url) {

    }

    /***
     * 监听 ZooKeeper 节点的子节点变化（新增、更新、删除）
     * @param path
     * @throws Exception
     */
    public void watchNodeDataChange(String path) throws Exception{
        PathChildrenCache cache = new PathChildrenCache(client,path,true);

        //启动PathChildrenCache
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        //添加PathChildrenCacheListener监听器
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                final PathChildrenCacheEvent.Type type = event.getType();
                System.out.println("PathChildren event: "+type);
                RpcEventData eventData = null;
                if(type.equals(CHILD_REMOVED)){
                    String path = event.getData().getPath();
                    final URL url = parsePath(path);
                    eventData = new DestroyEventData(url);

                }else if((type.equals(CHILD_UPDATED)) || type.equals(CHILD_ADDED)){
                    String path = event.getData().getPath();
                    byte[] bytes = client.getData().forPath(path);
                    Object o = JSON.parseObject(bytes,URL.class);
                    eventData = type.equals(CHILD_UPDATED) ? new UpdateRpcEventData(o) : new AddRpcEventData(o);
                }
                RpcListerLoader.sendEvent(eventData);
            }
        });
    }

    private String getProviderDataPath(URL url){
        return ROOT_PATH+PROVIDER+"/"+url.getServiceName()+"/"+url.getVersion()+"/"+url.getIp()+":"+url.getPort();
    }

    private String getProviderPath(URL url){
        return ROOT_PATH+PROVIDER+"/"+url.getServiceName()+"/"+url.getVersion();
    }

    private String getProviderPath(String serviceName,String version){
        return ROOT_PATH + PROVIDER + "/" + serviceName + "/" + version;
    }

    private URL parsePath(String path) throws Exception{
        final String[] split = path.split("/");
        String className = split[3];
        String version = split[4];

        final String[] split1 = split[5].split(":");
        String host = split1[0];
        String port = split1[1];

        final URL url = new URL();
        url.setServiceName(className);
        url.setVersion(version);
        url.setIp(host);
        url.setPort(Integer.parseInt(port));
        return url;
    }


    public boolean deleteNode(String path){
        try{
            client.delete().forPath(path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existNode(String path){
        try{
            Stat stat = client.checkExists().forPath(path);
            return stat != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
