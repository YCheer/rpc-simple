package fun.lzyan.compress.gzip;

import fun.lzyan.compress.Compress;

/**
 * @author lzyan
 * @description
 */
public class GzipCompress implements Compress {
    @Override
    public byte[] compress(byte[] bytes) {
        return new byte[0];
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        System.out.println("spi 调用");
        return new byte[0];
    }
}
