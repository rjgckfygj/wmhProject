package org.socket.serialization;
import org.common.constants.RpcSerialization;
import org.spi.ExtensionLoader;

import java.io.IOException;

public class SerializationFactory {

    public static org.socket.serialization.RpcSerialization get(RpcSerialization serialization){
        return ExtensionLoader.getInstance().get(serialization.name);
    }

    public static org.socket.serialization.RpcSerialization get(String name){
        return ExtensionLoader.getInstance().get(name);
    }

    public static void init() throws IOException, ClassNotFoundException {
        ExtensionLoader.getInstance().loadExtension(org.socket.serialization.RpcSerialization.class);
    }
}
