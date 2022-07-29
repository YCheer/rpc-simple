package fun.lzyan;

import fun.lzyan.test.client.HelloController;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * @author lzyan
 * @description
 */
public class TestProxy {

    HelloController helloController = new HelloController();

    @Test
    public void testProxy() {
        Class<?> targetClass = helloController.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            System.out.println(declaredField);
            System.out.println(declaredField.getType());
        }
    }
}
