package fun.lzyan.component.serialize;

import fun.lzyan.component.extension.SPI;

/**
 * @author lzyan
 * @description
 */
@SPI
public interface Serialize {


    byte[] serialize(Object obj);


    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
