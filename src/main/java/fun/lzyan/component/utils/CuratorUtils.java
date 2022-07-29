package fun.lzyan.component.utils;

import fun.lzyan.component.enums.RpcConfigEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
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
    // 注册路径
    public static final String ZK_REGISTER_ROOT_PATH = "/simple-rpc";
    // 
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    // 服务注册的地址
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();


    /**
     * 创建持久节点，与临时节点不同，持久节点在客户端断开连接时不会被移除
     *
     * @param zkClient
     * @param path
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("the node already exists. The node is :[{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("the node was created successfully. the node is :[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail ", path);
        }
    }

    /**
     * 获取当前节点的下一个子节点
     *
     * @param zkClient
     * @param rpcServiceName
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            log.error(" get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /**
     * 注册监听指定节点的变化
     *
     * @param rpcServiceName
     * @param zkClient
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddress = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddress);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }


    /**
     * 清空注册表
     *
     * @param zkClient
     * @param inetSocketAddress
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        // parallel()其实就是一个并行执行的流.它通过默认的ForkJoinPool, 可能提高你的多线程任务的速度，关于 parallel 的性能测试在测试用例中有描述
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
     *
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
