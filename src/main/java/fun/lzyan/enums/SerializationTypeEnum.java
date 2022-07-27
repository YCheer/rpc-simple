package fun.lzyan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lzyan
 * @description
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {

    KYRO((byte) 0x01, "kyro"),
    PROTOSTUFF((byte) 0x02, "protostuff"),
    HESSIAN((byte) 0x03, "hessian");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum value : SerializationTypeEnum.values()) {
            if (value.getCode() == code) {
                return value.name;
            }
        }
        return null;
    }

}
