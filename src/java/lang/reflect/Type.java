/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.lang.reflect;

/**
 * Type is the common superinterface for all types in the Java
 * programming language. These include raw types, parameterized types,
 * array types, type variables and primitive types.
 *
 * @since 1.5
 *
 *
 * https://www.cnblogs.com/baiqiantao/p/7460580.html
 */
// Type �� Java ����������������͵Ĺ������ӿڡ�
// ������������ƽ�������о���ʹ�õ� int��String��List��Map���������ͣ����Ǵ�Java���ԽǶ���˵���Ի������͡������������ϵĳ���
// Type��ϵ�е����Ͱ���:
//    - ԭʼ����(Class) ��������������ƽ����ָ���࣬������ö�١����顢ע���
//    - ����������(ParameterizedType) �������Ͳ��������ͣ�����˵�ķ��ͣ����� List<T>��Map<Integer, String>��List<? extends Number>, ʵ���� sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
//    - ������������(GenericArrayType) ����T[] ʵ���� sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl
//    - ���ͱ���(TypeVariable) ������������ ParameterizedType �е� E��K �����ͱ�������ʾ��ָ�κ���, ʵ���� sun.reflect.generics.reflectiveObjects.TypeVariableImpl
//    - ��������(Class) ���� Built-in �������ͣ����� int.class��char.class��void.class, Ҳ���� Wrappers �������Ͱ�װ���ͣ����� Integer.class��Boolean.class��Void.class
// Type �ӿڵ���һ���ӽӿ� WildcardType ����ͨ������ʽ���ͣ����ͱ��ʽ���ͣ�����?��? super T��? extends T���������� Java �����е�һ�֡�
public interface Type {
    /**
     * Returns a string describing this type, including information
     * about any type parameters.
     *
     * @implSpec The default implementation calls {@code toString}.
     *
     * @return a string describing this type
     * @since 1.8
     */
    // �������������͵��ַ����������й��κ����Ͳ�������Ϣ��
    default String getTypeName() {
        return toString();
    }
}
