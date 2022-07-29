package fun.lzyan.component.compress;

import fun.lzyan.component.extension.SPI;

/**
 * @author lzyan
 * @description
 */
@SPI
public interface Compress {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);

}
