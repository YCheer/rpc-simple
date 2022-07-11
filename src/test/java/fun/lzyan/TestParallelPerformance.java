package fun.lzyan;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

/**
 * @author lzyan
 * @description
 */
public class TestParallelPerformance {

    @Test
    public void testPerformance() {
        List<String> list = getLoopList();
        normalBeforeLoop(list);
        normalAfterLoop(list);
        notNormalAfterLoop(list);
    }

    private static void notNormalAfterLoop(List<String> list) {
        long a = System.currentTimeMillis();
        list.stream().parallel().forEach(System.out::print);
        System.out.println(" list.stream().parallel().forEach 执行耗时 : " + (System.currentTimeMillis() - a) / 1000f + " 秒 ");
    }

    private static void normalAfterLoop(List<String> list) {
        long a = System.currentTimeMillis();
        list.stream().forEach(System.out::print);
        System.out.println(" list.stream().forEach 执行耗时 : " + (System.currentTimeMillis() - a) / 1000f + " 秒 ");
        a = System.currentTimeMillis();
        list.forEach(System.out::print);
        System.out.println(" list.forEach 执行耗时 : " + (System.currentTimeMillis() - a) / 1000f + " 秒 ");
    }

    private static void normalBeforeLoop(List<String> list) {
        long a = System.currentTimeMillis();
        for (String s : list) {
            System.out.print(s);
        }
        System.out.println(" for each 执行耗时 : " + (System.currentTimeMillis() - a) / 1000f + " 秒 ");
    }

    private static List<String> getLoopList() {
        List<String> list = Lists.newArrayList();
        for (int i = 0; i < 10000; i++) {
            list.add("item " + i);
        }
        return list;
    }

}
