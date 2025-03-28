package org.event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 管理和调度 RPC（远程过程调用）事件监听器（IRpcLister），并根据事件类型将事件分发给相应的监听器
 * 1.注册监听器
 * 2.分发事件
 * 3.异步处理：使用线程池异步执行监听器的逻辑，避免阻塞主线程
 */
public class RpcListerLoader {
    //一个固定大小为 2 的线程池，用于异步处理事件监听器的执行逻辑
    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    //一个静态列表，用于存储所有注册的 IRpcLister 监听器
    private static List<IRpcLister> rpcListerList = new ArrayList<>();

    public void init(){
        registerLister(new AddRpcListener());
        registerLister(new DestroyRpcLister());
        registerLister(new UpdateRpcLister());
    }

    public static void registerLister(IRpcLister rpcLister){
        rpcListerList.add(rpcLister);
    }

    /**
     * 根据事件类型将事件分发给相应的监听器
     * @param eventData
     */
    public static void sendEvent(RpcEventData eventData){
        if(eventData == null){
            return;
        }
        if(!rpcListerList.isEmpty()){
            for(IRpcLister iRpcLister : rpcListerList){
                //调用 getInterfaceGenerics 方法获取监听器接口的泛型类型
                final Class<?> generics = getInterfaceGenerics(iRpcLister);

                //如果事件的类型（eventData.getClass()）与监听器的泛型类型匹配
                if(eventData.getClass().equals(generics)){
                    eventThreadPool.execute(()->{
                        iRpcLister.exec(eventData);       //使用线程池 eventThreadPool 异步执行监听器的 exec 方法
                    });
                }
            }
        }
    }

    /**
     * 通过反射获取 IRpcLister 接口的泛型类型
     * @param o
     * @return
     */
    public static Class<?> getInterfaceGenerics(Object o){
        //获取对象 o 的类实现的泛型接口（getGenericInterfaces）
        Type[] types = o.getClass().getGenericInterfaces();

        //将第一个接口的泛型类型（ParameterizedType）提取出来
        ParameterizedType parameterizedType = (ParameterizedType) types[0];

        //获取泛型参数的第一个类型（getActualTypeArguments()[0])
        Type type =parameterizedType.getActualTypeArguments()[0];

        //如果该类型是 Class 类型，则返回该类型；否则返回 null
        if(type instanceof Class<?>){
            return (Class<?>) type;
        }
        return null;
    }
}
