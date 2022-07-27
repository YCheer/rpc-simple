package fun.lzyan.handler;

import fun.lzyan.dto.RpcRequest;
import fun.lzyan.exception.RpcException;
import fun.lzyan.provider.ServiceProvider;
import fun.lzyan.provider.impl.ZkServiceProviderImpl;
import fun.lzyan.utils.SingletonFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 调用对应的方法，返回相应的结果
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
