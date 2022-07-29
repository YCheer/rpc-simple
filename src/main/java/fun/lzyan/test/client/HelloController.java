package fun.lzyan.test.client;

import fun.lzyan.component.spring.annotation.RpcReference;
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

    public void test() throws InterruptedException {
        String hello = helloService.hello(new Hello("111", "222"));
        log.info("return: {}", hello);
    }
}
