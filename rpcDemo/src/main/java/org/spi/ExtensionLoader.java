package org.spi;


import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一个单例模式的扩展加载器，用于加载和管理扩展组件.
 * 实现类似 SPI（Service Provider Interface） 的机制，允许用户通过配置文件动态加载和使用自定义的扩展实现
 */
public class ExtensionLoader {

    private static String SYS_EXTENSION_LOADER_DIR_PREFIX = "META-INF/xrpc/";
    private static String DIY_EXTENSION_LOADER_DIR_PREFIX = "META-INF/rpc/";

    private static String[] prefixs = {SYS_EXTENSION_LOADER_DIR_PREFIX,DIY_EXTENSION_LOADER_DIR_PREFIX};

    //用于缓存扩展类的 Class 对象。键是接口的全限定名，值是对应的实现类的 Class 对象
    private static Map<String,Class> extensionClassCache = new ConcurrentHashMap<>();
    private static Map<String,Map<String,Class>> extensionClassCaches = new ConcurrentHashMap<>();

    //用于缓存单例对象。键是接口的全限定名，值是对应的单例实例。
    private static Map<String,Object> singletonsObject = new ConcurrentHashMap<>();

    //extensionLoader 是一个静态变量，用于存储 ExtensionLoader 的唯一实例。
    private static ExtensionLoader extensionLoader;

    //静态代码块中，通过 new ExtensionLoader() 创建了一个实例，并将其赋值给 extensionLoader。
    //这种方式实现了单例模式，确保全局只有一个 ExtensionLoader 实例
    static {
        extensionLoader = new ExtensionLoader();
    }

    public static ExtensionLoader getInstance(){
        return extensionLoader;
    }
    private ExtensionLoader(){}

    /**
     * 获取bean
     * @param name
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public <V> V get(String name){
        System.out.println("name = "+name);
        if(!singletonsObject.containsKey(name)){
            try{
                //从 extensionClassCache 中获取与 name 对应的 Class 对象。
                //使用 Class.newInstance() 创建一个实例，并将其放入 singletonsObject 缓存中。
                singletonsObject.put(name,extensionClassCache.get(name).newInstance());
            }catch (InstantiationException e){
                e.printStackTrace();
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }
        }
        return (V) singletonsObject.get(name);
    }

    /**
     * 根据接口的 Class 类型，获取该接口的所有实现类实例
     * @param clazz
     * @return
     */
    public List gets(Class clazz){

        //获取接口的全限定名
        final String name = clazz.getName();

        //如果没有找到，抛出 ClassNotFoundException 并打印堆栈信息
        if(!extensionClassCaches.containsKey(name)){
            try{
                throw new ClassNotFoundException(clazz + "未找到");
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }


        final Map<String, Class> stringClassMap = extensionClassCaches.get(name);
        List<Object> objects = new ArrayList<>();
        if(stringClassMap.size() > 0){
            //遍历实现类映射，对每个实现类：
            stringClassMap.forEach((k,v)->{
                try{
                    //检查 singletonsObject 是否已经存在该类的单例实例。
                    //如果不存在，则通过 Class.newInstance() 创建一个实例。
                    //将实例添加到结果列表 objects 中。
                    objects.add(singletonsObject.getOrDefault(k,v.newInstance()));
                }catch (InstantiationException e){
                    e.printStackTrace();
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
            });
        }
        return objects;
    }

    /**
     * 加载指定接口的所有扩展实现，并将它们缓存到 extensionClassCache 和 extensionClassCaches 中
     * @param clazz
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadExtension(Class clazz) throws IOException,ClassNotFoundException{
        if(clazz == null){
            throw new IllegalArgumentException("class 没找到");
        }
        System.out.println("load Extesion: "+clazz.getName());

        //获取当前类的类加载器，用于加载配置文件和类
        ClassLoader classLoader = this.getClass().getClassLoader();

        //初始化缓存映射,用于存储当前接口的所有扩展实现类
        Map<String,Class> classMap = new HashMap<>();

        //从系统SPI以及用户SPI中找bean
        for(String prefix : prefixs){
            String spiFilePath = "META-INF/xrpc/" + clazz.getName();

            //使用类加载器的 getResources 方法获取所有匹配路径的资源（通常是文件）
            Enumeration<URL> enumeration = classLoader.getResources(spiFilePath);

            System.out.println("spiFilePath :"+spiFilePath);
            System.out.println("enumeration : "+enumeration.hasMoreElements());


            //读取配置文件内容
            while(enumeration.hasMoreElements()){
                URL url  = enumeration.nextElement();

                //使用 InputStreamReader 和 BufferedReader 读取文件内容
                InputStreamReader inputStreamReader = null;
                inputStreamReader = new InputStreamReader(url.openStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;

                //按行读取配置文件，每行的格式为 key=value
                while((line = bufferedReader.readLine()) != null){
                    String [] lineArr = line.split("=");
                    String key = lineArr[0];
                    String name = lineArr[1];

                    //使用 Class.forName(name) 加载实现类
                    final Class<?> aClass = Class.forName(name);
                    extensionClassCache.put(key,aClass);
                    System.out.println("key = "+key+"    aClass.name= "+aClass.getName());
                    classMap.put(key,aClass);
                }
            }

        }
        //将当前接口的所有扩展实现类（存储在 classMap 中）存入 extensionClassCaches 中，键为接口的全限定名
        extensionClassCaches.put(clazz.getName(),classMap);
    }
}
