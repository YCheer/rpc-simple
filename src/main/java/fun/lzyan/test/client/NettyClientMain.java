package fun.lzyan.test.client;

import fun.lzyan.component.spring.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author lzyan
 * @description
 */
@RpcScan(basePackage = {"fun.lzyan"}) // 自定义扫描规则
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");

        helloController.testInvokeHello();
        helloController.testInvokeWorld();
        helloController.testInvokeBye();
    }
}
