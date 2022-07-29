package fun.lzyan.component.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lzyan
 * @description
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {

    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        // 遍历所有枚举
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

}
