package fun.lzyan.component.spring.annotation;

import java.lang.annotation.*;

/**
 * rpc服务注解，标注在服务的实现类上，意在简化流程，标注的实现类直接发布到注册中心
 * 实现逻辑是在标记有@RpcService的服务实现类在实例化和依赖注入完成后在显示调用初始化方法的前进行发布注册
 * 
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

