package fun.lzyan.component.utils.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建 ThreadPool 线程池的工具类
 *
 * @author lzyan
 * @description
 */
@Slf4j
public class ThreadPoolFactoryUtil {

    /**
     * 通过 threadNamePrefix 来区分不同线程池（把相同的 threadNamePrefix 的线程池看作是同一业务场景服务）
     * key：threadNamePrefix
     * value：threadPool
     */
    private static final Map<String, ExecutorService> THREAD_POOS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil() {
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    private static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix) {
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    private static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNameFix, Boolean daemon) {
        // computeIfAbsent()方法，对 hashmap 中指定 key 进行重新计算，如果不存在这个 key，则添加到 hashmap 中，第二个参数就是添加的 value
        ExecutorService threadPool = THREAD_POOS.computeIfAbsent(threadNameFix, k -> createThreadPool(customThreadPoolConfig, threadNameFix, daemon));
        // 如果 threadPool 被 shutdown 的话就重新创建一个 
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            THREAD_POOS.remove(threadNameFix);
            threadPool = createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNameFix, daemon);
            THREAD_POOS.put(threadNameFix, threadPool);
        }
        return threadPool;
    }


    /**
     * 创建线程池
     *
     * @param customThreadPoolConfig 线程池的配置
     * @param threadNameFix
     * @param daemon
     * @return
     */
    private static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNameFix, Boolean daemon) {
        // 获取创建线程的工厂
        ThreadFactory threadFactory = createThreadFactory(threadNameFix, daemon);
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(), customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(), customThreadPoolConfig.getUnit(), customThreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    /**
     * 创建 ThreadFactory ，如果threadNamePrefix不为空，则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param threadNameFix 创建的线程名字的前缀
     * @param daemon        指定是否为 Daemon Thread (守护线程)
     * @return
     */
    public static ThreadFactory createThreadFactory(String threadNameFix, Boolean daemon) {
        if (threadNameFix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNameFix + "-%d")
                        .setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNameFix + "-%d").build();
            }
        }
        // 返回一个用于创建新线程的默认线程工厂
        return Executors.defaultThreadFactory();
    }


    /**
     * shutdown 所有的线程池
     */
    public static void shutDownAllThreadPool() {
        THREAD_POOS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdownNow();
            log.info("shut down thread pool [{}] [{}] ", entry.getKey(), executorService.isTerminated());
            // 到达指定时间的 10s 后，还有线程没有执行完，不再等待，关闭线程池
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool never terminated");
                executorService.shutdownNow();
            }
        });
    }


    /**
     * 打印线程池的状态
     *
     * @param threadPool
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        // ScheduledThreadPoolExecutor 能做一些基本的定时任务。若是真正需要上需求的一般都用第三方框架quartz、xxx-job之类的了
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        // 延迟 0 秒，每隔1秒后执行
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("==========ThreadPool Status==========");
            log.info("ThreadPool size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads:[{}]", threadPool.getActiveCount());
            log.info("Number of Tasks:[{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue:[{}]", threadPool.getQueue().size());
            log.info("=====================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
