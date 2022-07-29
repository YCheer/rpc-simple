package fun.lzyan.component.exception;

import fun.lzyan.component.enums.RpcErrorMessageEnum;

/**
 * 自定义rpc调用异常
 *
 * @author lzyan
 * @description
 */
public class RpcException extends RuntimeException {

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }

}
