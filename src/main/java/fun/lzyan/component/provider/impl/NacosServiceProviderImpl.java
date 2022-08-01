package fun.lzyan.component.provider.impl;

import fun.lzyan.component.config.RpcServiceConfig;
import fun.lzyan.component.enums.RpcErrorMessageEnum;
import fun.lzyan.component.exception.RpcException;
import fun.lzyan.component.extension.ExtensionLoader;
import fun.lzyan.component.extension.SPI;
import fun.lzyan.component.netty.server.NettyRpcServer;
import fun.lzyan.component.provider.ServiceProvider;
import fun.lzyan.component.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import javax.xml.ws.Service;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static fun.lzyan.component.netty.server.NettyRpcServer.REGISTER_CENTER;

/**
 * @author lzyan
 * @description
 */
@Slf4j
public class NacosServiceProviderImpl implements ServiceProvider {

    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public NacosServiceProviderImpl() {
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
        this.serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(REGISTER_CENTER);
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publicService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }

    }
}
