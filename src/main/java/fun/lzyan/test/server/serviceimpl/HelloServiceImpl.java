package fun.lzyan.test.server.serviceimpl;

import fun.lzyan.test.serviceapi.Hello;
import fun.lzyan.test.serviceapi.HelloService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lzyan
 * @description
 */
@Slf4j
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl 收到:{}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl 返回:{}", result);
        return result;
    }
}
