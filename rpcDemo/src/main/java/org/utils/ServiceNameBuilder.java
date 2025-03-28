package org.utils;

/***
 * 构建服务相关的名称或信息
 */
public class ServiceNameBuilder {
    /**
     * 将服务名称（serviceName）和服务版本（serviceVersion）拼接成一个字符串，中间用$符号分隔。
     * @param serviceName
     * @param serviceVersion
     * @return
     */
    public static String builderServiceKey(String serviceName,String serviceVersion){

        return String.join("$",serviceName,serviceVersion);
    }


    /**
     * 将服务标识符（key）、IP地址（ip）和端口号（port）拼接成一个字符串，中间用#符号分隔
     * @param key
     * @param ip
     * @param port
     * @return
     */
    public static String buildServiceNodeInfo(String key,String ip,Integer port){
        return String.join("#",key,ip,String.valueOf(port));
    }
}
