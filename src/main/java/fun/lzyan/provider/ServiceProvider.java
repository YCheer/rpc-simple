package fun.lzyan.provider;

import fun.lzyan.config.RpcServiceConfig;

/**
 * 
 * 存储和提供服务的对象   
 * 
 * @author lzyan
 * @description
 */
public interface ServiceProvider {

    
    void addService(RpcServiceConfig rpcServiceConfig);
    
    /**
     * rpc service name 
     * @param rpcServiceName
     * @return
     */
    Object getService(String rpcServiceName);

    /**
     * 
     * @param rpcServiceConfig
     */
    void publicService(RpcServiceConfig rpcServiceConfig);

}
