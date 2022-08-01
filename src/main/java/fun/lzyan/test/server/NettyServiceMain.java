package fun.lzyan.test.server;

import fun.lzyan.component.netty.server.NettyRpcServer;
import fun.lzyan.component.spring.annotation.RpcScan;
import fun.lzyan.test.server.serviceimpl.ByeServiceImpl;
import fun.lzyan.test.server.serviceimpl.HelloServiceImpl;
import fun.lzyan.test.serviceapi.ByeService;
import fun.lzyan.test.serviceapi.HelloService;
import fun.lzyan.component.config.RpcServiceConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author lzyan
 * @description
 */
@RpcScan(basePackage = {"fun.lzyan"})
public class NettyServiceMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServiceMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");

        HelloService helloService = new HelloServiceImpl();
        ByeService byeService = new ByeServiceImpl();
        RpcServiceConfig helloRpcServiceConfig = RpcServiceConfig.builder()
                .group("test1").version("version1").service(helloService).build();
        RpcServiceConfig ByeRpcServiceConfig = RpcServiceConfig.builder()
                .group("test1").version("version1").service(byeService).build();
        
        nettyRpcServer.registerService(helloRpcServiceConfig);
        nettyRpcServer.registerService(ByeRpcServiceConfig);
        
        nettyRpcServer.start();
    }

}
