package fun.lzyan.test.client;

import fun.lzyan.component.spring.annotation.RpcReference;
import fun.lzyan.test.serviceapi.Bye;
import fun.lzyan.test.serviceapi.ByeService;
import fun.lzyan.test.serviceapi.Hello;
import fun.lzyan.test.serviceapi.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author lzyan
 * @description
 */
@Component
@Slf4j
public class HelloController {

    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService;

    @RpcReference(version = "version1", group = "test1")
    private ByeService byeService;

    public void testInvokeHello() throws InterruptedException {
        String hello = helloService.hello(new Hello("hello", "hello!"));
        log.info("return: {}", hello);
    }

    public void testInvokeWorld() {
        String world = helloService.world(new Hello("world", "world!"));
        log.info("return: {}", world);
    }

    public void testInvokeBye() {
        String bye = byeService.bye(new Bye("bye", "bye!"));
        log.info("return:{}", bye);
    }
}
