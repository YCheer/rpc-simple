package fun.lzyan.netty.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义协议解码器
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
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
        // lengthFieldOffset: 魔数是4B，版本是1B，然后是全长，所以值为5 
        // lengthFieldLength: 全长是4B，所以值为4
        // lengthAdjustment: 全长包括所有数据并读取前9个字节，所以左边的长度是（fullLength-9），所以值为-9
        // initialBytesToStrip: 将动手检查魔数和版本，所以不要剥离任何字节，所以值为0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * 
     * @param maxFrameLength 最大帧长度，它决定了可以接收的最大数据长度，如果超过，数据将被丢弃
     * @param lengthFieldOffset 长度字段偏移，长度字段是跳过指定字节长度的字段
     * @param lengthFieldLength 长度字段中的字节数
     * @param lengthAdjustment 添加到长度字段值的补偿值
     * @param initialBytesToStrip 跳过的字节数
     *                            如果需要接收所有的 header+body数据，这个值为0
     *                            如果你只想接收body数据，那么你需要跳过header消耗的字节数
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

}
