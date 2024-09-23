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
 * A common interface for all entities that declare type variables.
 *
 * @since 1.5
 */
// 可以声明类型变量的实体的父接口，也就是说，只有实现了该接口才能在对应的实体上声明(定义)类型变量(K, V)
// 这些实体目前只有三个：Class（类）、Construstor（构造器）、Method（方法），
// GenericDeclaration的直接实现子类没有Field类，所以属性上面不能定义类型变量。
public interface GenericDeclaration extends AnnotatedElement {
    /**
     * Returns an array of {@code TypeVariable} objects that
     * represent the type variables declared by the generic
     * declaration represented by this {@code GenericDeclaration}
     * object, in declaration order.  Returns an array of length 0 if
     * the underlying generic declaration declares no type variables.
     *
     * @return an array of {@code TypeVariable} objects that represent
     *     the type variables declared by this generic declaration
     * @throws GenericSignatureFormatError if the generic
     *     signature of this generic declaration does not conform to
     *     the format specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     */
    // 返回实体上声明（定义）的所有的类型变量。
    public TypeVariable<?>[] getTypeParameters();
}
