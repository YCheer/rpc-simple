package fun.lzyan.component.spring.annotation;

import java.lang.annotation.*;

/**
 * @author lzyan
 * @description
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    /**
     * service version
     *
     * @return
     */
    String version() default "";

    /**
     * service group
     *
     * @return
     */
    String group() default "";
}
