package fun.lzyan.component.registry;

import fun.lzyan.component.dto.RpcRequest;
import fun.lzyan.component.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author lzyan
 * @description
 */
@SPI
public interface ServiceDiscovery {

    /**
     * 通过 rpcServiceName 查找服务
     * @param rpcRequest
     * @return
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);

}
