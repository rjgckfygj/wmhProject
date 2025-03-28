package org.router;

import org.common.URL;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器
 *
 */
public class RoundRobinLoadBalancer implements LoadBalancer{
    /**
     * AtomicInteger：一个线程安全的整数类，用于实现原子操作。
     * roundRobinId：用于记录当前的轮询索引。每次选择一个服务实例时，该索引会递增。
     * static：表示该变量是类级别的，所有实例共享同一个 roundRobinId。
     * new AtomicInteger(0)：初始化为 0，表示从第一个服务实例开始。
     */
    private static AtomicInteger roundRobinId = new AtomicInteger(0);

    @Override
    public URL select(List<URL> urls) {
        roundRobinId.addAndGet(1);
        if(roundRobinId.get() == Integer.MAX_VALUE){
            roundRobinId.set(0);
        }
        //计算当前索引对服务实例数量取模，确保索引在 0 到 urls.size() - 1 之间。
        return urls.get(roundRobinId.get() % urls.size());
    }
}
