package org.register;

import org.common.constants.Register;
import org.spi.ExtensionLoader;

import java.io.IOException;

public class RegistryFactory {

    public static RegistryService get(Register register) {
        System.out.println("register name: "+register.name);
        return ExtensionLoader.getInstance().get(register.name);
    }

    public static RegistryService get(String name){return ExtensionLoader.getInstance().get(name);}

    public static void init() throws IOException,ClassNotFoundException{
        ExtensionLoader.getInstance().loadExtension(RegistryService.class);
    }
}
