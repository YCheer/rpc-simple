package fun.lzyan.test.serviceapi;

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
public class Hello {
    private String message;
    private String description;
    
}
