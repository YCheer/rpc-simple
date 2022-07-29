package fun.lzyan.component.config;

import fun.lzyan.component.netty.server.NettyRpcServer;
import fun.lzyan.component.utils.CuratorUtils;
import fun.lzyan.component.utils.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * 自定义关闭钩子
 * 当服务器关闭时需要做的一些事情，比如取消注册所的服务
 *
 * @author lzyan
 * @description
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        // Runtime.getRuntime().addShutdownHook() 这个方法是在jvm中增加一个关闭的钩子，当jvm关闭的时候
        // 会执行系统中设置的所有通过 addShutdownHook 添加的钩子，当系统执行完这些钩子之后，jvm才会关闭
        // 所以这些钩子可以在 jvm 关闭的时候进行内存清理、对象销毁等操作

        // 通过 Runtime 实例，使得应用程序和其运行环境相连接。Runtime 是在应用启动期间自建立的，应用程序不能够创建 Runtime
        // 但是可以通过 Runtime.getRuntime() 来获取当前应用对象 Runtime 对象引用，通过该引用可以获得当前运行环境的相关信息，比如空闲内存、最大内存
        // 以及为当前虚拟机添加关闭钩子 addShutdownHook 等

        // 关闭钩子本身就是一个线程，也叫hook线程，用来监听jvm的关闭，所以hook线程会延迟jvm的关闭时间，所以尽量减少执行的时间
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // 返回封装的地址
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException ignored) {
            }
            // 关闭所有 线程池
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }

}
