package tests.java.lang.reflect;

import org.junit.Test;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hst
 * @create 2024-09-19 16:19
 * @Description:
 *
 * https://www.cnblogs.com/baiqiantao/p/7460580.html
 */
public class TypeTest {


    @Test
    public void testClass() throws NoSuchMethodException {
        new TypeTest().showType();
    }


    private void showType() throws NoSuchMethodException {
        // ע�� int.class �� Integer.class �ǲ�һ����(û����ν���Զ�װ�䡢�Զ��������)�����ܻ���
        Class<?> clazz = List.class;
        Method method = TypeTest.class.getMethod("testType", int.class, Boolean.class, clazz, clazz, clazz, clazz, clazz, Map.class);
        Type[] genericParameterTypes = method.getGenericParameterTypes(); //���շ�����������˳�򷵻ز����� Type ����
        for (Type type : genericParameterTypes) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] types = parameterizedType.getActualTypeArguments(); //���ر�ʾ�����͡�ʵ�����Ͳ������� Type ����
                for (int i = 0; i < types.length; i++) {
                    System.out.println(i + getTypeInfo(types[i]));
                }
            } else {
                System.out.println("  " + getTypeInfo(type));
            }
        }
    }

    private String getTypeInfo(Type type) {
        String typeName = type.getTypeName();
        Class<?> clazz = type.getClass();
        Class<?>[] interfaces = clazz.getInterfaces();
        StringBuilder typeInterface = new StringBuilder();
        for (Class<?> clazzType : interfaces) {
            typeInterface.append(clazzType.getSimpleName()).append(",");
        }
        return "��" + typeName + "��    ��" + clazz.getSimpleName() + "��    ��" + typeInterface + "��";
    }

    public <T> void testType(int i, Boolean b, List<String> a1, List<ArrayList<String>> a2, List<T> a3, //
                             List<? extends Number> a4, List<ArrayList<String>[]> a5, Map<Boolean, Integer> a6) {
    }



    @Test
    public void testParameterizedType() throws NoSuchMethodException {
        Method method = TypeTest.class.getMethod("testType", Map.Entry.class);
        ParameterizedType parameterizedType = (ParameterizedType) method.getGenericParameterTypes()[0];

        System.out.println("getOwnerType  " + getTypeInfo(parameterizedType.getOwnerType()));
        System.out.println("getRawType  " + getTypeInfo(parameterizedType.getRawType()));

        Type[] types = parameterizedType.getActualTypeArguments();
        for (Type type : types) {
            System.out.println(getTypeInfo(type));
        }
    }

    public <T> void testType(Map.Entry<String, T> mapEntry) {
    }


    @Test
    public void testTypeVariable() throws NoSuchMethodException {
        Method method = TypeTest.class.getMethod("testType");
        TypeVariable<?>[] typeVariables = method.getTypeParameters(); //���ط��������� TypeVariable ����

        for (int i = 0; i < typeVariables.length; i++) {
            TypeVariable<?> typeVariable = typeVariables[i];
            Type[] bounds = typeVariable.getBounds();
            GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration(); //��public void Test.test()��
            boolean isSameObj = genericDeclaration.getTypeParameters()[i] == typeVariable; // true����ͬһ������

            System.out.println(getTypeInfo(typeVariable));
            for (Type type : bounds) {
                System.out.println("    " + getTypeInfo(type));
            }
        }
    }

    public <T extends List<String>, U extends Integer, Int> void testType() {
    }

    @Test
    public void testGenericArrayType() throws NoSuchMethodException {
        Method method = TypeTest.class.getMethod("testType", Object[].class, String[].class, List.class);
        Type[] types = method.getGenericParameterTypes(); //���շ�����������˳�򷵻ز����� Type ����
        for (Type type : types) {
            System.out.println(getTypeInfo(type));
            if (type instanceof GenericArrayType) {
                GenericArrayType genericArrayType = (GenericArrayType) type;
                System.out.println("    " + getTypeInfo(genericArrayType.getGenericComponentType()));
            }
        }
    }

    // ֻ�е�һ�������ǡ��������顿����
    public <T> void testType(T[] a1, String[] a2, List<T> a3) {
    }


    @Test
    public void testWildcardType() throws NoSuchMethodException {
        Method method = TypeTest.class.getMethod("testType", List.class, List.class, List.class, List.class);
        Type[] types = method.getGenericParameterTypes(); //���շ�����������˳�򷵻ز����� Type ����
        for (Type type : types) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments(); //���ر�ʾ�����͡�ʵ�����Ͳ������� Type ����
            if (actualTypeArguments[0] instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) actualTypeArguments[0];
                System.out.println("��ͨ�������" + getTypeInfo(wildcardType));
                for (Type upperType : wildcardType.getUpperBounds()) {
                    System.out.println("  upperType" + getTypeInfo(upperType));
                }
                for (Type lowerType : wildcardType.getLowerBounds()) {
                    System.out.println("  lowerType" + getTypeInfo(lowerType));
                }
            } else {
                System.out.println("��ͨ�������" + getTypeInfo(actualTypeArguments[0]));
            }
        }
    }

    public <T> void testType(List<T> a1, List<?> a2, List<? extends T> a3, List<? super Integer> a4) {
    }
}
