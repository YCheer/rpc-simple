package fun.lzyan.component.registry.zk;

import fun.lzyan.component.registry.ServiceRegistry;
import fun.lzyan.component.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @author lzyan
 * @description
 */
public class ZkServiceRegistryImpl implements ServiceRegistry {


    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
