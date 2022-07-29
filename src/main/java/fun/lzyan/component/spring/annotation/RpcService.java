package fun.lzyan.component.spring.annotation;

import java.lang.annotation.*;

/**
 * @author lzyan
 * @description
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {

    String version() default "";

    String group() default "";

}

