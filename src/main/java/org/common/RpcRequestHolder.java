package org.common;
import org.socket.codec.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 它用于管理 RPC（远程过程调用）请求的唯一标识（请求ID）以及与请求相关的 RpcFuture<RpcResponse> 对象。
 * 作用1：生成唯一的请求ID
 * 作用2：管理请求与响应的映射：通过一个 Map 将请求ID与对应的 RpcFuture<RpcResponse> 对象关联起来，方便在处理响应时找到对应的请求。
 */
public class RpcRequestHolder {
    //请求id
    //类型：AtomicLong，一个线程安全的长整型变量。
    //作用：用于生成唯一的请求ID。
    //初始值：从 0 开始。
    //线程安全：AtomicLong 确保在多线程环境下，ID的生成是线程安全的，不会出现重复的ID。
    public final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    //绑定请求
    //类型：ConcurrentHashMap，一个线程安全的哈希表。
    //作用：存储请求ID与 RpcFuture<RpcResponse> 对象的映射关系。
    //线程安全：ConcurrentHashMap 确保在多线程环境下，对映射的读写操作是安全的。
    //映射关系：
    //Key：请求ID（Long 类型）。
    //Value：RpcFuture<RpcResponse> 对象，表示与该请求相关的异步响应。
    public static final Map<Long,RpcFuture<RpcResponse>> REQUEST_MAP = new ConcurrentHashMap();

    /**
     * 如果当前的请求ID已经达到long类型的最大值，则将ID重置为0，避免溢出。
     * 使用 incrementAndGet() 方法将当前ID加1，并返回新的ID。
     * @return
     */
    public static Long getRequestId(){
        if(REQUEST_ID_GEN.longValue() == Long.MAX_VALUE){
            REQUEST_ID_GEN.set(0);
        }
        return REQUEST_ID_GEN.incrementAndGet();
    }
}
