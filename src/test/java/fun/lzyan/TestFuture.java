package fun.lzyan;

import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

/**
 * @author lzyan
 * @description
 */
public class TestFuture {

    @Test
    @SneakyThrows
    public void testFuture() {
        Integer integer = 2;
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        completableFuture.complete(integer);
        Integer i = completableFuture.get();
        System.out.println(i);
    }
    


}
