package fun.lzyan.extension;

import fun.lzyan.compress.Compress;
import fun.lzyan.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 参考 Dubbo SPI的实现原理
 * https://dubbo.apache.org/zh/docs/v2.7/dev/source/dubbo-spi/#m-zhdocsv27devsourcedubbo-spi
 * <p>
 *     
 * 调用过程总结
 * 通过传入 @SPI 注解标记的接口调用 getExtensionLoader() 方法，获取这个接口的 ExtensionLoader对象，
 * 通过 ExtensionLoader 对象调用 getExtension() 方法来获取这个接口的扩展类/实现类的实例对象
 * 在 getExtension() 方法中获取扩展类/实现类的过程中分为以下步骤
 * 1. 首先获取接口扩展类/实现类的持有者，即 Holder
 * 为什么会有这个持有者，我的理解是因为这个接口会有多个扩展类/实现类，而用这个Holder维护一个Map，通过配置文件中配置的形式是key-value
 * 直接使用它作为map的key-value（当然这个value是Class类型的）来存储这个接口的多个扩展类/实现类
 * 2. 然后通过指定的配置文件中的key，调用 createExtension()，来创建对应的实例对象
 * 在 createExtension() 方法中创建过程分为以下步骤
 * 1. 在当前接口的持有者中，获取指定的扩展类 getExtensionClasses()
 * 2. 根据获取的扩展类/实现类，获取它的实例对象
 * 在 getExtensionClasses() 方法获取过程中缓存没有的话就去解析META-INF下的配置文件了，解析的过程中就是解析指定的接口的配置文件
 * 然后对文件的内容逐行解析，保存在相应的缓存Map即可。
 * 
 * @author lzyan
 * @description
 */
@Slf4j
public final class ExtensionLoader<T> {

    // 存放扩展类的配置文件目录
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    // 缓存 extension loader
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    // 扩展类/实现类 的实例
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    // 创建实例的时候指定 class
    private final Class<?> type;
    // Holder 实例持有者，有多个。也就是一个接口，就有一个 Holder 所以是 map
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    // 扩展类/实现类 的缓存
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();


    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        // 接口需要添加 @SPI 这个注解
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        // 首先从缓存中获取，未命中的话就创建
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    public T getExtension(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Exception name could not be null or empty");
        }
        // 首先从缓存中获取，未命中的话就创建，对象持有者
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // 如果实例不存在，就创建一个单例
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                }

            }
        }
        return (T) instance;
    }

    private T createExtension(String name) {
        // 从文件中加载所有类型为 T （注意这是与ExtensionLoader的泛型一样） 的扩展类并按名称获取特定的扩展类
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }
        // 获取这个扩展类的实例，没有就创建
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                // newInstance 方法实际上就是使用对应类的无参构造方法来创建该类的实例，等价于 Parent parent = new Parent()
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    public Map<String, Class<?>> getExtensionClasses() {
        // 从缓存中获取加载的扩展类
        Map<String, Class<?>> classes = cachedClasses.get();
        // 双重检测
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // 从扩展文件中加载所有的扩展
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 从 directory 中加载每个文件
     * 再从每个文件中加载文件中的内容
     * 提取内容，根据内容配置的实现类，loadClass 加载实现类
     *
     * @param extensionClass
     */
    private void loadDirectory(Map<String, Class<?>> extensionClass) {
        // 从文件路径中获取问价的内容，并加载
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        log.info("load extension from extensions directory:" + fileName);
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClass, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 读取文件内容，提取每行内容 gzip=fun.lzyan.compress.gzip.GzipCompress
     * 然后对于配置内容的每个实现类进行加载 loadClass
     *
     * @param extensionClass
     * @param classLoader
     * @param resourceUrl
     */
    private void loadResource(Map<String, Class<?>> extensionClass, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // 每次读取每一行
            while ((line = reader.readLine()) != null) {
                //  每行出现 # 的索引
                final int ci = line.indexOf('#');
                if (ci > 0) {
                    // # 后面的是注释，所以忽略
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        // gzip=fun.lzyan.compress.gzip.GzipCompress
                        // 截取 = 前面的
                        String name = line.substring(0, ei).trim();
                        // 截取 = 后面的
                        String clazzName = line.substring(ei + 1).trim();
                        // 参考 Dubbo 实现的 spi 是一个键值对，所以它们都不能为空
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClass.put(name, clazz);
                        } else {
                            log.error("file key-value format error");
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
