package fun.lzyan;

import com.alibaba.nacos.api.exception.NacosException;
import fun.lzyan.component.utils.NacosUtil;
import org.junit.Test;

import java.net.InetSocketAddress;

/**
 * @author lzyan
 * @description
 */
public class TestNacos {

    @Test
    public void testNacos() {
        try {
            NacosUtil.registerService("test_service", new InetSocketAddress("127.0.0.1", 8888));
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

}
