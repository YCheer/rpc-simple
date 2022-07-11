package fun.lzyan.utils.threadpool;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author lzyan
 * @description
 */
@Getter
@Setter
public class CustomThreadPoolConfig {

    /**
     * 线程池默认参数
     */
    private static final int DEFAULT_CORE_POOL_SIZE = 10;
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    private static final int DEFAULT_KEEP_ALIVE_TIME = 1;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;
    private static final int DEFAULT_BLOCKING_QUEUE_CAPACITY = 100;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    /**
     * 可配置参数
     */
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
    private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    private TimeUnit unit = DEFAULT_TIME_UNIT;

    /**
     * 使用有界队列（阻塞队列）
     * 
     * 在队列为空的时候，获取元素的线程会等待队列变为非空。当队列满的时候，存储元素的线程会等待队列可用。
     * 阻塞队列，常用于生产者和消费者的场景，生产者是往队列里添加元素的线程，消费者是从队列里面拿元素的线程。
     * 所以，阻塞队列就是生产者存放元素的容器，而消费者就是从容器中拿元素
     */
    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);

}
