package fun.lzyan.compress;

import fun.lzyan.extension.SPI;

/**
 * @author lzyan
 * @description
 */
@SPI
public interface Compress {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);

}
