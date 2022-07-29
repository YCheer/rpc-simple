package fun.lzyan.component.spring;

import fun.lzyan.component.config.RpcServiceConfig;
import fun.lzyan.component.extension.ExtensionLoader;
import fun.lzyan.component.netty.RpcRequestTransport;
import fun.lzyan.component.provider.ServiceProvider;
import fun.lzyan.component.provider.impl.ZkServiceProviderImpl;
import fun.lzyan.component.proxy.RpcClientProxy;
import fun.lzyan.component.spring.annotation.RpcReference;
import fun.lzyan.component.utils.SingletonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 受 spring 管理的 bean，在创建 bean 之前/之后 调用此方法来处理一些注解
 *
 * @author lzyan
 * @description
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        // netty client
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        // 变量
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                
                // 值为 true 指示反射的对象在使用时应该取消 Java 语言访问检查
                declaredField.setAccessible(true);
                try {
                    // 为属性设置实例
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
