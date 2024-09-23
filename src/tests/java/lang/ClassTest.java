package tests.java.lang;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLOutput;

import static org.junit.Assert.assertTrue;

/**
 * @author hst
 * @create 2024-09-18 14:36
 * @Description:
 */
public class ClassTest {

    @Test
    public void testGetClassInstance() throws ClassNotFoundException {
        // ͨ�����.class ���Ի�ȡclass����
        Class<?> classOne = String.class;
        // ͨ������ʱ��Ķ��󣬵���getClass()
        Class<?> classTwo = new String().getClass();
        // ͨ��Class�ľ�̬���� Class.forName(String className) (ȫ·��)
        Class<?> classThree = Class.forName("java.lang.String");
        // ͨ�������������ȫ·��
        ClassLoader classLoader = ClassTest.class.getClassLoader();
        Class<?> classFour = classLoader.loadClass("java.lang.String");
        assertTrue(classOne == classTwo);
        assertTrue(classOne == classThree);
        assertTrue(classOne == classFour);
    }

    @Test
    public void testGetClassMethod() throws InstantiationException, IllegalAccessException {
        Class<String> stringClass = String.class;
        String name = stringClass.getName();
        System.out.println(name);
        String simpleName = stringClass.getSimpleName();
        System.out.println(simpleName);
        boolean isInterface = stringClass.isInterface();
        System.out.println(isInterface);
        boolean array = stringClass.isArray();
        System.out.println(array);
        boolean primitive = stringClass.isPrimitive();
        System.out.println(primitive);
        boolean enumType = stringClass.isEnum();
        System.out.println(enumType);
        Class<? super String> superclass = stringClass.getSuperclass();
        System.out.println(superclass);
        Class<?>[] interfaces = stringClass.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            System.out.println(anInterface);
        }
        Field[] declaredField = stringClass.getDeclaredFields();
        for (Field field : declaredField) {
            System.out.println(field);
        }
        String instance = stringClass.newInstance();
        System.out.println(instance.getClass());
        int modifiers = stringClass.getModifiers();
        System.out.println(Modifier.toString(modifiers));
        Annotation[] annotations = stringClass.getAnnotations();
        for (Annotation annotation : annotations) {
            System.out.println(annotation);
        }
    }

}
