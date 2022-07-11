package fun.lzyan;

import fun.lzyan.utils.threadpool.ThreadPoolFactoryUtil;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lzyan
 * @description
 */
public class TestThreadPool {

    @Test
    public void testThreadPool() throws InterruptedException {
//        ExecutorService executorService = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("lzyanfun");
//        Thread.sleep(5000);
//        ThreadPoolFactoryUtil.printThreadPoolStatus((ThreadPoolExecutor) executorService);
        System.out.println(Runtime.getRuntime().availableProcessors());
    }
}
