package tests.java.lang.reflect;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertTrue;

/**
 * @author hst
 * @create 2024-09-18 14:36
 * @Description:
 */
public class ClassTest {

    @Test
    public void testGetClassInstance() throws ClassNotFoundException {
        // 通过类的.class 属性获取class对象
        Class<?> classOne = String.class;
        // 通过运行时类的对象，调用getClass()
        Class<?> classTwo = new String().getClass();
        // 通过Class的静态方法 Class.forName(String className) (全路径)
        Class<?> classThree = Class.forName("java.lang.String");
        // 通过类加载器加载全路径
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
