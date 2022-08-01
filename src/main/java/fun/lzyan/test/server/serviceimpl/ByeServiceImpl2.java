package fun.lzyan.test.server.serviceimpl;

import fun.lzyan.component.spring.annotation.RpcService;
import fun.lzyan.test.serviceapi.Bye;
import fun.lzyan.test.serviceapi.ByeService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lzyan
 * @description
 */
@RpcService(version = "version2", group = "test2")
@Slf4j
public class ByeServiceImpl2 implements ByeService {
    
    static {
        log.info("ByeServiceImpl2 被初始化了");
    }
    
    @Override
    public String bye(Bye bye) {
        log.info("bye2被调用");
        return bye.getDescription();
    }
}
