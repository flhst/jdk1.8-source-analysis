package tests.java.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author hst
 * @create 2024-10-01 1:09
 * @Description:
 */
public class ArrayListTest {

    @Test
    public void testToArray() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> iterator = list.iterator();
        list.remove("c");
        Object[] array = list.toArray();
        System.out.println(Arrays.toString(array));
    }

}
