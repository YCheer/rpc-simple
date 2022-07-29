package fun.lzyan;

import org.junit.Test;

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
