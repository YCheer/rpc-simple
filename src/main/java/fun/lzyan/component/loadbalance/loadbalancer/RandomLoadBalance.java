package fun.lzyan.component.loadbalance.loadbalancer;

import fun.lzyan.component.dto.RpcRequest;
import fun.lzyan.component.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * @author lzyan
 * @description
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected String doSelect(List<String> serviceAddress, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddress.get(random.nextInt(serviceAddress.size()));
    }
}
