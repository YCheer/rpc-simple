package fun.lzyan.component.loadbalance;

import fun.lzyan.component.dto.RpcRequest;

import java.util.List;

/**
 * @author lzyan
 * @description
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {
        if (serviceUrlList == null || serviceUrlList.isEmpty()) {
            return null;
        }
        if (serviceUrlList.size() == 1) {
            return serviceUrlList.get(0);
        }
        return doSelect(serviceUrlList, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddress, RpcRequest rpcRequest);
}
