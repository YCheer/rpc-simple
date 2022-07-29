package fun.lzyan.component.loadbalance;

import fun.lzyan.component.dto.RpcRequest;
import fun.lzyan.component.extension.SPI;

import java.util.List;

/**
 * @author lzyan
 * @description
 */
@SPI
public interface LoadBalance {

    /**
     * 从现有服务地址列表中选择一个
     * @param serviceUrlList 服务地址列表
     * @param rpcRequest 
     * @return 目标服务地址
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
