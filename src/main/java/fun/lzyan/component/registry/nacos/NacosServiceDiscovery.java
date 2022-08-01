package fun.lzyan.component.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import fun.lzyan.component.dto.RpcRequest;

import fun.lzyan.component.registry.ServiceDiscovery;
import fun.lzyan.component.utils.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

/**
 * @author lzyan
 * @description
 */
@Slf4j
public class NacosServiceDiscovery implements ServiceDiscovery {


    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        try {
            List<Instance> instances = NacosUtil.getAllInstance(rpcRequest.getRpcServiceName());
            if (instances.size() == 0) {
                log.error("service not found {}", rpcRequest.getRpcServiceName());
            }
            // 直接作（负载均衡，随机），偷懒
            Instance instance = instances.get(new Random().nextInt(instances.size()));
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return null;
    }
}
