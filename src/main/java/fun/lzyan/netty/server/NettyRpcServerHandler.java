package fun.lzyan.netty.server;

import fun.lzyan.dto.RpcMessage;
import fun.lzyan.dto.RpcRequest;
import fun.lzyan.dto.RpcResponse;
import fun.lzyan.enums.CompressTypeEnum;
import fun.lzyan.enums.RpcResponseCodeEnum;
import fun.lzyan.enums.SerializationTypeEnum;
import fun.lzyan.handler.RpcRequestHandler;
import fun.lzyan.netty.codec.RpcConstants;
import fun.lzyan.utils.SingletonFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义服务端的ChannelHandler来处理客户端发送的数据
 * <p>
 * 如果继承自 SimpleChannelInBoundHandler 的话就不要考虑 ByteBuf 的释放，{@link io.netty.channel.SimpleChannelInboundHandler} 内部的
 * ChannelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄漏问题。
 *
 * @author lzyan
 * @description
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                log.info("server receive msg:[{}]", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    // 执行目标方法（客户端需要执行的方法）并返回结果
                    Object result = rpcRequestHandler.handler(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcResponse.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            // 确保释放ByteBuff，否则可能出现内存泄漏问题
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理心跳检测
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            // 设置的是 30s 的心跳检测机制
            // 如果 30s 内没有收到 channel 的数据，会触发一个 IdleState#READER_IDLE 事件
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen , so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常捕捉机制
     * <p>
     * exceptionCaught 只会 catch inbound handler 的 exception，outbound exception 需要在 writeAndFlush 方法里加上 listener 来监听消息是否发送成功
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
