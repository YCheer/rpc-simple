package fun.lzyan.component.registry;

import fun.lzyan.component.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册
 *
 * @author lzyan
 * @description
 */
@SPI
public interface ServiceRegistry {

    /**
     * register service
     *
     * @param rpcServiceName
     * @param inetSocketAddress
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
