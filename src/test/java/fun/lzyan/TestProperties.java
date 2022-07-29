package fun.lzyan;

import fun.lzyan.component.netty.server.NettyRpcServer;
import fun.lzyan.component.utils.CuratorUtils;
import fun.lzyan.component.utils.StringUtil;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author lzyan
 * @description
 */
public class TestProperties {

    @Test
    public void testProperties() {
        // /D:/project/JavaWebProject/RPC/rpc-simple/target/test-classes/
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        System.out.println(url.getPath());
    }

    @Test
    public void testGetPath() {
        CuratorUtils.getZkClient();
    }

    @Test
    public void testIPAddress() {
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
            System.out.println(inetSocketAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMagicNumber() {
    }
}
