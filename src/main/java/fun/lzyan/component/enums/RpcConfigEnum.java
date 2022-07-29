package fun.lzyan.component.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lzyan
 * @description
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {

    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
