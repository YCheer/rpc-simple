package fun.lzyan.registry;

import fun.lzyan.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @author lzyan
 * @description
 */
public interface ServiceDiscovery {

    /**
     * 通过 rpcServiceName 查找服务
     * @param rpcRequest
     * @return
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);

}
