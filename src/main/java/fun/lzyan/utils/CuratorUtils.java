package fun.lzyan.utils;

import fun.lzyan.enums.RpcConfigEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * zookeeper 客户端工具
 *
 * @author lzyan
 * @description
 */
@Slf4j
public class CuratorUtils {

    // 睡眠时间 ms
    private static final int BASE_SLEEP_TIME = 1000;
    // 重试的次数
    private static final int MAX_RETRIES = 3;
    // 默认连接
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "192.168.119.129:2181";
    //
    private static CuratorFramework zkClient;
    // 服务注册的地址
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();



    /**
     * 清空注册表
     * @param zkClient
     * @param inetSocketAddress
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        // parallel()其实就是一个并行执行的流.它通过默认的ForkJoinPool, 可能提高你的多线程任务的速度，关于parallel的性能测试在test中
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if (p.endsWith(inetSocketAddress.toString())) {
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                log.error("clear registry for path [{}] fail", p);
            }
        });
        log.info("All registered services on the server are cleared: [{}]", REGISTERED_PATH_SET.toString());
    }

    /**
     * 获取 zookeeper 连接客户端
     * @return
     */
    public static CuratorFramework getZkClient() {
        // 检查用户是否设置了 zk 的地址 说实在，在段感觉写的优点绕，感觉没什么必要。先从配置文件拿，配置文件没有，就从本地拿
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS;
        // 如果已经启动，就直接返回
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // 重试策略，重拾三次，会增加重试之间的休眠时间
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // 阻塞等待30s直到连接到zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to zk");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }
}
