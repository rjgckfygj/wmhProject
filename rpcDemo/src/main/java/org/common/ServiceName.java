package org.common;

import java.util.Objects;

/**
 * 封装服务名称
 */
public class ServiceName {
    private final String name; //final 确保类的不可变性
    private final String version;

    public ServiceName(String name,String version)
    {
        this.name = name;
        this.version = version;
    }

    @Override
    public  String toString(){
        return "ServiceName{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    //重写了 equals 方法，用于定义对象之间的相等性。
    @Override
    public boolean equals(Object o){
        //如果两个对象引用相同（this == o），直接返回 true。
        if(this == o) return true;

        //如果传入的对象为 null 或者不是 ServiceName 类型（getClass() != o.getClass()），返回 false
        if(o == null ||getClass() != o.getClass()) return false;

        //将传入的对象强制转换为 ServiceName 类型，并比较它们的 name 字段是否相等（通过 Objects.equals 方法）。
        ServiceName that =(ServiceName) o;
        return Objects.equals(name,that.name) && Objects.equals(version,that.version);
    }

    //重写了 hashCode 方法，用于计算对象的哈希码。
    @Override
    public int hashCode(){
        //哈希码是基于 name 字段生成的，使用了 Objects.hash 方法。
        //当对象被用作哈希表的键（如在 HashMap 或 HashSet 中）时，正确的 hashCode 实现可以确保对象能够被正确地存储和检索。
        return Objects.hash(name,version);
    }
}
