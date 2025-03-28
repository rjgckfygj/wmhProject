package org.socket.serialization.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.socket.serialization.RpcSerialization;

import java.io.IOException;

public class JsonSerialization implements RpcSerialization {
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        try{
            String jsonString = JSON.toJSONString(obj);
            return jsonString.getBytes("UTF-8");
        }catch (JSONException e){
            throw new IOException("Error serializing object to JSON",e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        try{
            String jsonString = new String(data,"UTF-8");
            return JSON.parseObject(jsonString,clz);
        }catch (JSONException e){
            throw new IOException("Error deserializing JSON to object",e);
        }
    }
}
