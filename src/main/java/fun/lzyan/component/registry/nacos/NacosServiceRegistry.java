package fun.lzyan.component.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import fun.lzyan.component.enums.RpcErrorMessageEnum;
import fun.lzyan.component.exception.RpcException;
import fun.lzyan.component.registry.ServiceRegistry;
import fun.lzyan.component.utils.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author lzyan
 * @description
 */
@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        try {
            NacosUtil.registerService(rpcServiceName, inetSocketAddress);
        } catch (NacosException e) {
            log.error("register service error:", e);
            throw new RpcException(RpcErrorMessageEnum.REGISTER_SERVICE_FAILED);
        }
    }
}
