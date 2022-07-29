package fun.lzyan.component.config;

import lombok.*;

/**
 * @author lzyan
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    
    /**
     * 服务版本
     */
    private String version = "";

    /**
     * 当接口有多个实现类时，按组区分
     */
    private String group = "";

    /**
     * 目标 service
     */
    private Object service; 

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }

}
