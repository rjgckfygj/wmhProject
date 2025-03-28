package org.filter;

import java.io.IOException;
import java.util.List;

import org.filter.client.ClientAfterFilter;
import org.filter.client.ClientBeforeFilter;
import org.filter.server.ServerAfterFilter;
import org.filter.server.ServerBeforeFilter;
import org.spi.ExtensionLoader;


public class FilterFactory {


    public static List<Filter> getClientBeforeFilters() {
        return ExtensionLoader.getInstance().gets(ClientBeforeFilter.class);

    }

    public static List<Filter> getClientAfterFilters() {
        return ExtensionLoader.getInstance().gets(ClientAfterFilter.class);

    }

    public static List<Filter> getServerBeforeFilters() {
        return ExtensionLoader.getInstance().gets(ServerBeforeFilter.class);

    }

    public static List<Filter> getServerAfterFilters() {
        return ExtensionLoader.getInstance().gets(ServerAfterFilter.class);
    }


    public static void initClient() throws IOException, ClassNotFoundException {
        System.out.println("FileFactory initClient");
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ClientAfterFilter.class);
        extensionLoader.loadExtension(ClientBeforeFilter.class);
        System.out.println("*********getClientBeforeFilters :"+getClientBeforeFilters());
    }

    public static void initServer() throws IOException, ClassNotFoundException {
        System.out.println("FileFactory initServer");
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ServerAfterFilter.class);
        extensionLoader.loadExtension(ServerBeforeFilter.class);
    }
}
