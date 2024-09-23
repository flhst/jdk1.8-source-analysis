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
     * Integer �ǲ��ɱ�ģ������޸���ֵ
     * @see java.lang.Integer#value
     */
    @Test
    public void testIntegerFinalValueImmutability() {
        Integer num = 10;
        // �ⲻ���޸�ԭ���Ķ��󣬶�����numָ��һ���µ�Integer����
        num = 20;
        assertTrue(20 == num);
    }

    /**
     * ͨ�������޸�Integer��ֵ
     * @see java.lang.Integer#value
     * @throws NoSuchFieldException ����û��ָ�����Ƶ��ֶΡ�
     * @throws IllegalAccessException ��ǰ����ִ�еķ�����Ȩ����ָ���ࡢ�ֶΡ��������캯��
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
     * ͨ���ṩԭ�Ӳ����������� AtomicInteger �޸�ֵ
     * @see java.util.concurrent.atomic.AtomicInteger
     */
    @Test
    public void testModifyIntegerValueByAutomicInteger() {
        AtomicInteger atomicInteger = new AtomicInteger(10);
        atomicInteger.set(20);
        assertTrue(20 == atomicInteger.get());
    }


}
