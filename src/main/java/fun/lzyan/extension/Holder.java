package fun.lzyan.extension;

/**
 * 用于持有目标对象
 * 
 * @author lzyan
 * @description
 */
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

}
