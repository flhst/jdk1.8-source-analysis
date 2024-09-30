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
// Type 是 Java 编程语言中所有类型的公共父接口。
// 它并不是我们平常工作中经常使用的 int、String、List、Map等数据类型，而是从Java语言角度来说，对基本类型、引用类型向上的抽象
// Type体系中的类型包括:
//    - 原始类型(Class) 不仅仅包含我们平常所指的类，还包括枚举、数组、注解等
//    - 参数化类型(ParameterizedType) 带有类型参数的类型，即常说的泛型，例如 List<T>、Map<Integer, String>、List<? extends Number>, 实现类 sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
//    - 泛型数组类型(GenericArrayType) 例如T[] 实现类 sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl
//    - 类型变量(TypeVariable) 即参数化类型 ParameterizedType 中的 E、K 等类型变量，表示泛指任何类, 实现类 sun.reflect.generics.reflectiveObjects.TypeVariableImpl
//    - 基本类型(Class) 包括 Built-in 内置类型，例如 int.class、char.class、void.class, 也包括 Wrappers 内置类型包装类型，例如 Integer.class、Boolean.class、Void.class
// Type 接口的另一个子接口 WildcardType 代表通配符表达式类型，或泛型表达式类型，比如?、? super T、? extends T，他并不是 Java 类型中的一种。
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
    // 返回描述此类型的字符串，包括有关任何类型参数的信息。
    default String getTypeName() {
        return toString();
    }
}
