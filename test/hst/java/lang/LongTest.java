package hst.java.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author hst
 * @create 2024-11-16 12:55
 * @Description:
 */
public class LongTest {
    @Test
    public void parseUnsignedLongTest() {
        // assertEquals(Long.parseUnsignedLong("0"), 0L);
        // assertEquals(Long.parseUnsignedLong(Long.toString(Long.MAX_VALUE)), Long.MAX_VALUE);
        System.out.println(Long.valueOf("00110100", 2));
        System.out.println(Long.highestOneBit(Long.valueOf("00110100", 2)));
    }
}
