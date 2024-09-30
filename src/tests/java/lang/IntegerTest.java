package tests.java.lang;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author hst
 * @create 2024-09-18 12:33
 * @Description:
 */
public class IntegerTest {

    /**
     * Integer 是不可变的，不能修改其值
     * @see java.lang.Integer#value
     */
    @Test
    public void testIntegerFinalValueImmutability() {
        Integer num = 10;
        // 这不是修改原来的对象，而是让num指向一个新的Integer对象
        num = 20;
        assertTrue(20 == num);
    }

    /**
     * 通过反射修改Integer的值
     * @see java.lang.Integer#value
     * @throws NoSuchFieldException 该类没有指定名称的字段。
     * @throws IllegalAccessException 当前正在执行的方法无权访问指定类、字段、方法或构造函数
     */
    @Test
    public void testModifyIntegerValueByReflection() throws NoSuchFieldException, IllegalAccessException {
        Integer num = 10;
        Field value = Integer.class.getDeclaredField("value");
        value.setAccessible(true);
        value.set(num, 20);
        assertTrue(20 == num);
    }

    /**
     * 通过提供原子操作的整数类 AtomicInteger 修改值
     * @see java.util.concurrent.atomic.AtomicInteger
     */
    @Test
    public void testModifyIntegerValueByAutomicInteger() {
        AtomicInteger atomicInteger = new AtomicInteger(10);
        atomicInteger.set(20);
        assertTrue(20 == atomicInteger.get());
    }


}
