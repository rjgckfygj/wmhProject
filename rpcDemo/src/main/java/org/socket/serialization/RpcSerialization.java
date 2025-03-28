package org.socket.serialization;

import java.io.IOException;

/**
 * 用于实现序列化和反序列化功能
 * 序列化是将对象转换为字节数组的过程
 * 反序列化则是将字节数组还原为对象的过程。
 */
public interface RpcSerialization {
    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;
}
