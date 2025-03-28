package org.router;

import java.io.IOException;

import org.common.constants.LoadBalance;
import org.spi.ExtensionLoader;

public class LoadBalancerFactory {


    public static LoadBalancer get(LoadBalance loadBalance){

        return ExtensionLoader.getInstance().get(loadBalance.name);
    }

    public static LoadBalancer get(String name){
        return ExtensionLoader.getInstance().get(name);
    }

    public static void init() throws IOException,ClassNotFoundException{
        ExtensionLoader.getInstance().loadExtension(LoadBalancer.class);
    }
}
