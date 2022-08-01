package fun.lzyan.test.server.serviceimpl;

import fun.lzyan.test.serviceapi.Bye;
import fun.lzyan.test.serviceapi.ByeService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lzyan
 * @description
 */
@Slf4j
public class ByeServiceImpl implements ByeService {
    
    @Override
    public String bye(Bye bye) {
        log.info("bye 被调用");  
        return bye.getDescription();
    }
    
}
