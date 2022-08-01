package fun.lzyan.component.netty.server;

import fun.lzyan.component.config.CustomShutdownHook;
import fun.lzyan.component.config.RpcServiceConfig;
import fun.lzyan.component.extension.ExtensionLoader;
import fun.lzyan.component.netty.codec.RpcMessageDecoder;
import fun.lzyan.component.netty.codec.RpcMessageEncoder;
import fun.lzyan.component.provider.ServiceProvider;
import fun.lzyan.component.provider.impl.NacosServiceProviderImpl;
import fun.lzyan.component.provider.impl.ZkServiceProviderImpl;
import fun.lzyan.component.utils.RuntimeUtil;
import fun.lzyan.component.utils.SingletonFactory;
import fun.lzyan.component.utils.threadpool.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * netty服务端，接收客户端消息，根据客户端消息调用对应方法和返回结果给客户端。
 *
 * @author lzyan
 * @description
 */
@Slf4j
@Component
public class NettyRpcServer {

    public static final int PORT = 9999;

    // nacos / zk
    public static String REGISTER_CENTER = "nacos";

    private final ServiceProvider serviceProvider = ExtensionLoader.getExtensionLoader(ServiceProvider.class).getExtension(REGISTER_CENTER);

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publicService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start() {
        // 注册一个钩子，这个钩子在jvm关闭的时候就会调用，取消注册所有服务
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        // 获取本机地址
        String host = InetAddress.getLocalHost().getHostAddress();

        // Netty 服务器创建过程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // 为了提升性能，如果用户实现的 ChannelHandler 包含复杂或者可能导致同步阻塞的业务逻辑，往往需要通过线程池来提升并发能力
        // 线程池添加有两种策略：用户自定义线程池执行业务 ChannelHandler，以及通过 Netty 的 EventExecutorGroup 机制来并行执行 ChannelHandler
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2, ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
        );
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP 默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据块，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启了 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，可以适当调整大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 设置 NioServerSocketChannel 的处理器
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 设置连入服务端的 Client 的 SocketChannel 的处理器，当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 30 秒之内没有收到客户端的请求就关闭连接（触发一个 IdleState#READER_IDLE事件）
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));

                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());

                            p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });
            // 绑定端口，同步等待绑定成功
            ChannelFuture f = b.bind(host, PORT).sync();
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }

    }

}
