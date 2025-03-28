package org.socket.serialization.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.socket.serialization.RpcSerialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

/***
 * 基于 Kryo 库的序列化和反序列化工具类
 */
public class KryoSerialization implements RpcSerialization {
    private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>(){
        @Override
        protected Kryo initialValue(){
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            return kryo;
        }
    };
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        Output output = null;
        try{
            Kryo kryo = kryos.get();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            output = new Output(byteArrayOutputStream);
            kryo.writeClassAndObject(output,obj);
            return output.toBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        Input input = null;
        try {
            Kryo kryo = kryos.get();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            input = new Input(byteArrayInputStream);
            return (T) kryo.readClassAndObject(input);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            if(input != null){
                input.close();
            }
        }
    }
}
