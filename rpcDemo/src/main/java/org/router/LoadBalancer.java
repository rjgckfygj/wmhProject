package org.router;

import org.common.URL;

import java.util.List;

public interface LoadBalancer {

    URL select(List<URL> urls);
}
