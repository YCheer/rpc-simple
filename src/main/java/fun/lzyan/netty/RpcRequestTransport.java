package fun.lzyan.netty;

import fun.lzyan.dto.RpcRequest;

/**
 * @author lzyan
 * @description
 */
public interface RpcRequestTransport {

    /**
     * 向服务器发送rpc请求，并获取结果
     * @param rpcRequest
     * @return
     */
    Object sendRpcRequest(RpcRequest rpcRequest);

}
