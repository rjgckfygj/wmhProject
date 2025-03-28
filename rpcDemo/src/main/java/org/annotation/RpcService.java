package org.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;

/**
 * 定义了一个名为 RpcService 的注解（Annotation）
 * 用于标记类（Type）级别的元数据，通常用于 RPC（远程过程调用）相关的服务定义
 */

//RetentionPolicy.RUNTIME：表示这个注解在运行时仍然可用，可以通过反射获取其值。
// 这意味着注解的信息会在类加载到 JVM 后仍然保留，可以在运行时通过反射 API 访问注解的值
@Retention(RetentionPolicy.RUNTIME)  //指定了注解的保留策略

//TYPE：表示这个注解只能应用于类、接口或枚举类型上。换句话说，它不能用于方法、字段或其他更细粒度的元素。
@Target(value = {TYPE})   //指定了注解可以应用的目标元素类型。
public @interface RpcService {

    /**
     * 指定实现方,默认为实现接口中第一个
     * @return
     */
    Class<?> serviceInterface() default void.class;


    String version() default "1.0";
}
