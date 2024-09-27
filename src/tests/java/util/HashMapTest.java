package tests.java.util;

import org.junit.Test;

import java.util.HashMap;
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

}
