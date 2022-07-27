package fun.lzyan.serialize;

import fun.lzyan.extension.SPI;

/**
 * @author lzyan
 * @description
 */
@SPI
public interface Serialize {


    byte[] serialize(Object obj);


    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
