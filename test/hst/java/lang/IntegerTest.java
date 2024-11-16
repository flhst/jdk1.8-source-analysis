package hst.java.lang;

import org.junit.Test;

import java.math.BigInteger;

/**
 * @author hst
 * @create 2024-11-16 13:34
 * @Description:
 */
public class IntegerTest {

    @Test
    public void maxValueTest() {
        System.out.println((int) Long.parseLong(Integer.toBinaryString(Integer.MIN_VALUE), 2));
        System.out.println(Integer.valueOf(Integer.toBinaryString(Integer.MAX_VALUE), 2));
    }

}
