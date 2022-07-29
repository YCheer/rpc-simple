package fun.lzyan.component.utils;

/**
 * @author lzyan
 * @description
 */
public class RuntimeUtil {
    /**
     * 获取cpu的核心数
     *
     * @return
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
    
}
