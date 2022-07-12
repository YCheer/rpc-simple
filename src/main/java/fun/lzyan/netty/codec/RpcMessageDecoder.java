package fun.lzyan.netty.codec;

import fun.lzyan.compress.Compress;
import fun.lzyan.dto.RpcMessage;
import fun.lzyan.enums.CompressTypeEnum;
import fun.lzyan.extension.ExtensionLoader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 自定义协议解码器
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId      |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 * <p>
 * {@link LengthFieldBasedFrameDecoder} 基于长度字段的编码器，在发送消息前，先约定用定长字节表示接下来发送的数据长度。每一条消息分为消息头head和消息体body
 * 在消息头中包含消息总长度的字段，然后进行业务逻辑的处理。用来解决 Tcp 的粘包和半包问题
 * </p>
 *
 * @author lzyan
 * @description
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        // lengthFieldOffset: 魔数是4B，版本是1B，然后是全长，所以值为 magic code + version = 5 
        // lengthFieldLength: 全长是4B，所以值为 full length = 4
        // lengthAdjustment: full length 包括所有数据并读取前 9（magic code + version + full length） 个字节，所以左边的长度是（full Length-9），所以值为-9
        // initialBytesToStrip: 手动检查魔数和版本，所以不要剥离任何字节，所以值为0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength      最大帧长度，它决定了可以接收的最大数据长度，如果帧的长度大于这个值，就会抛出 TooLongFrameException，数据被丢弃
     * @param lengthFieldOffset   长度字段偏移，长度字段是跳过指定字节长度的字段
     * @param lengthFieldLength   长度字段中的字节数
     * @param lengthAdjustment    添加到长度字段值的补偿值
     * @param initialBytesToStrip 从解码帧中剥离的第一个字节数，跳过的字节数（通常用来跳过数据包包头，只解析正文）
     *                            如果需要接收所有的 header+body数据，这个值为0
     *                            如果你只想接收body数据，那么你需要跳过header消耗的字节数
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 根据构造函数设定的参数，从 ByteBuf 创建获取一个 frame
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decode;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                } finally {
                    frame.release();
                }
            }
        }
        return decode;
    }

    private Object decodeFrame(ByteBuf in) {
        // 按顺序读取 ByteBuf : 因为读取过的内容，就属于废弃部分了，再读只能读那些尚未读取的部分
        // 1.检查魔数
        checkMagicNumber(in);
        // 2.检查版本
        checkVersion(in);
        // 3.构建 RpcMessage 对象
        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();
        // 心跳检测
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        // 获取 body 的长度
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            // 4.读取 body 数据 
            in.readBytes(bs);
            // 解压字节
            String compressName = CompressTypeEnum.getName(compressType);
            ExtensionLoader.getExtensionLoader(Compress.class);

        }

        return null;
    }

    /**
     * 检查版本
     *
     * @param in
     */
    private void checkVersion(ByteBuf in) {
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    /**
     * 检查魔数
     * 跟约定好的魔数作对比
     *
     * @param in
     */
    private void checkMagicNumber(ByteBuf in) {
        // 读取前 4 位，然后作比较。
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code:" + Arrays.toString(tmp));
            }
        }
    }
}
