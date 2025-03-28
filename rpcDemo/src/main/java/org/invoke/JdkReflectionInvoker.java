package org.invoke;

import org.common.Cache;
import org.utils.ServiceNameBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用JDK反射机制来执行RPC请
 * 缓存方法调用信息
 */
public class JdkReflectionInvoker implements Invoker{
    private Map<Integer,MethodInvocation> methodCache = new HashMap<>();

    @Override
    public Object invoke(Invocation invocation) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        final Integer methodCode = invocation.getMethodCode();
        if(!methodCache.containsKey(methodCode)){
            final String key = ServiceNameBuilder.builderServiceKey(invocation.getClassName(),invocation.getServiceVersion());
            Object bean = Cache.SERVICE_MAP.get(key);
            final Class<?> aClass = bean.getClass();

            final Method method = aClass.getMethod(invocation.getMethodName(),invocation.getParameterTypes());
            methodCache.put(methodCode,new MethodInvocation(aClass.newInstance(),method));

        }
        final MethodInvocation methodInvocation = methodCache.get(methodCode);
        return methodInvocation.invoke(invocation.getParameter());
    }
}
