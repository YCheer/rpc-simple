package fun.lzyan.component.netty;

import fun.lzyan.component.dto.RpcRequest;
import fun.lzyan.component.extension.SPI;

/**
 * @author lzyan
 * @description
 */
@SPI
public interface RpcRequestTransport {

    /**
     * 向服务器发送rpc请求，并获取结果
     *
     * @param rpcRequest
     * @return
     */
    Object sendRpcRequest(RpcRequest rpcRequest);

}
