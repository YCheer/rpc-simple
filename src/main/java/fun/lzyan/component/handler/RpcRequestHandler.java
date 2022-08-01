package fun.lzyan.component.handler;

import fun.lzyan.component.dto.RpcRequest;
import fun.lzyan.component.exception.RpcException;
import fun.lzyan.component.extension.ExtensionLoader;
import fun.lzyan.component.provider.ServiceProvider;
import fun.lzyan.component.provider.impl.NacosServiceProviderImpl;
import fun.lzyan.component.provider.impl.ZkServiceProviderImpl;
import fun.lzyan.component.utils.SingletonFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.Extension;

import static fun.lzyan.component.netty.server.NettyRpcServer.REGISTER_CENTER;

/**
 * RpcRequest 处理器
 *
 * @author lzyan
 * @description
 */
@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = ExtensionLoader.getExtensionLoader(ServiceProvider.class).getExtension(REGISTER_CENTER);
    }

    /**
     * 调用对应的方法，返回相应的结果
     *
     * @param rpcRequest
     * @return
     */
    public Object handler(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 获取方法的执行结果
     *
     * @param rpcRequest 客户端请求
     * @param service    服务对象
     * @return 目标方法的执行结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method :[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }


}
