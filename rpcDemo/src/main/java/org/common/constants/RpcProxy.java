package org.common.constants;

public enum RpcProxy {
    //CG_LIB表示一种代理类型，其值为字符串"cglib"，通常用于表示使用CGLIB代理框架
    CG_LIB("cglib");

    public  String name;
    RpcProxy(String type){this.name = type;}

    public static RpcProxy get(String type){
        //遍历RpcProxy的所有枚举实例（通过values()方法获取）
        for(RpcProxy value : values()){
            if(value.name.equals(type)){
                return value;
            }
        }
        return null;
    }
}
