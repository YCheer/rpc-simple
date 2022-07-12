package fun.lzyan.utils;

/**
 * String 工具类
 *
 * @author lzyan
 * @description
 */
public class StringUtil {

    public static boolean isBlank(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            // 判断字符是否为空白字符 空白符包含：空格、tab键、换行符。有个疑问？ 这里不是判断第一个字符不是空白符的时候这个方法就走掉了么
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
