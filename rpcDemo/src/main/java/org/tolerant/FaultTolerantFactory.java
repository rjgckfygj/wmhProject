package org.tolerant;

import org.common.constants.FaultTolerant;
import org.spi.ExtensionLoader;

import java.io.IOException;

/**
 * 工厂类，用于创建和管理容错策略对象。
 */
public class FaultTolerantFactory {
//    private static Map<FaultTolerant,FaultTolerantStrategy> faultTolerantStrategyMap = new HashMap<>();

    /**
     * 静态代码块，它会在类加载时执行一次。
     * 初始化faultTolerantStrategyMap，将两种容错策略（Failover和FailFast）分别与对应的实现类关联起来：
     * FaultTolerant.Failover映射到FailoverFaultTolerantStrategy类。
     * FaultTolerant.FailFast映射到FailFastFaultTolerantStrategy类。
     */
//    static {
//        faultTolerantStrategyMap.put(FaultTolerant.Failover,new FailoverFaultTolerantStrategy());
//        faultTolerantStrategyMap.put(FaultTolerant.FailFast,new FailFastFaultTolerantStrategy());
//    }

    public static FaultTolerantStrategy get(FaultTolerant faultTolerant){
        return ExtensionLoader.getInstance().get(faultTolerant.name);
    }
    public static FaultTolerantStrategy get(String name){
        return ExtensionLoader.getInstance().get(name);
    }

    public static void init() throws IOException,ClassNotFoundException{
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(FaultTolerantStrategy.class);
    }
}
