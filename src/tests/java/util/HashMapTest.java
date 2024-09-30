package tests.java.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author hst
 * @create 2024-09-27 15:46
 * @Description:
 */
public class HashMapTest {

    @Test
    public void testReplaceAllBiFunction() {
        Map<Integer, String> map = new HashMap<>();
        Map<Integer, String> res = new HashMap<>();
        map.put(1, "one");
        map.put(2, "two");
        map.replaceAll((key, value) -> key + "-" + value);
        System.out.println(map);
        res.put(1, "1-one");
        res.put(2, "2-two");
        assertThat(map).isEqualTo(res);
    }

    @Test
    public void testComputeIfAbsent() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "one");
        map.computeIfAbsent(2, k -> "two");
        assertThat(map.get(2)).isEqualTo("two");
        System.out.println(map.computeIfAbsent(1, k -> "three"));
    }

    // 报错ConcurrentModificationException
    @Test
    public void testKeySet() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            System.out.println(next);
            map.put("d", 4);
        }
    }

    @Test
    public void testToString() {
        Map<Object, String> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("age", "25");
        map.put(map, "This is the map itself");
        System.out.println(map);
    }

}
