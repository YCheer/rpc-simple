package fun.lzyan.component.proxy;

import fun.lzyan.component.config.RpcServiceConfig;
import fun.lzyan.component.dto.RpcRequest;
import fun.lzyan.component.dto.RpcResponse;
import fun.lzyan.component.enums.RpcErrorMessageEnum;
import fun.lzyan.component.enums.RpcResponseCodeEnum;
import fun.lzyan.component.exception.RpcException;
import fun.lzyan.component.netty.RpcRequestTransport;
import fun.lzyan.component.netty.client.NettyRpcClient;
import fun.lzyan.test.client.NettyClientMain;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 动态代理类
 * 当一个动态代理对象调用一个方法时，它实际上调用了下面的invoke方法
 * 正因为动态代理，客户端调用远程方法就像调用本地方法一样（中间过程屏蔽）
 *
 * @author lzyan
 * @description
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private static final String INTERFACE_NAME = "interfaceName";

    /**
     * 用于向服务器发送请求。
     */
    private final RpcRequestTransport rpcRequestTransport;

    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = new RpcServiceConfig();
    }

    /**
     * 获取代理对象
     * <p>
     * 利用Java的反射技术，在运行时创建一个实现某些给定接口的新类也就是动态代理类，以及其实例对象
     * 代理的是接口，不是类，也不是抽象类，在运行时才知道具体的是实现，spring aop的原理亦是如此
     * <p>
     * newProxyInstance方法的三个参数
     * loader: 使用哪个类加载器去加载代理对象
     * interfaces：动态代理需要实现的接口
     * h：动态代理对象方法执行时，会调用h里面的invoke方法去执行
     *
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked") // 该注解的作用是 被批注的代码元素内部的某些警告保持静默
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 当你使用代理对象的时候，实际上会调用此方法。代理对象就是你通过 getProxy()方法得到的对象
     * 就如demo中，当调用 HelloService#hello() 方法的时候就会调用
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        RpcResponse<Object> rpcResponse = null;
        if (rpcRequestTransport instanceof NettyRpcClient) {
            CompletableFuture<RpcResponse<Object>> completableFuture = (CompletableFuture<RpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completableFuture.get();
        }
        this.check(rpcResponse, rpcRequest);
        
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

    }
}
