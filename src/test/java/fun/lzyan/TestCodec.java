package fun.lzyan;

import fun.lzyan.enums.CompressTypeEnum;
import fun.lzyan.netty.codec.RpcConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Test;

/**
 * @author lzyan
 * @description
 */
public class TestCodec {

    @Test
    public void testByteBuf() {
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        out.writeBytes(RpcConstants.MAGIC_NUMBER);
        out.writeByte(RpcConstants.VERSION);
        out.writerIndex(out.writerIndex() + 4);
        out.writeByte(CompressTypeEnum.GZIP.getCode());
        int i = out.writerIndex();
        System.out.println("i:" + i);
        log(out);
    }

    private static void log(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity());
        System.out.println(buf.toString());
    }
}
