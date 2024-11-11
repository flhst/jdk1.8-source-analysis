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

package java.lang.annotation;

/**
 * The constants of this enumerated type provide a simple classification of the
 * syntactic locations where annotations may appear in a Java program. These
 * constants are used in {@link Target java.lang.annotation.Target}
 * meta-annotations to specify where it is legal to write annotations of a
 * given type.
 *
 * <p>The syntactic locations where annotations may appear are split into
 * <em>declaration contexts</em> , where annotations apply to declarations, and
 * <em>type contexts</em> , where annotations apply to types used in
 * declarations and expressions.
 *
 * <p>The constants {@link #ANNOTATION_TYPE} , {@link #CONSTRUCTOR} , {@link
 * #FIELD} , {@link #LOCAL_VARIABLE} , {@link #METHOD} , {@link #PACKAGE} ,
 * {@link #PARAMETER} , {@link #TYPE} , and {@link #TYPE_PARAMETER} correspond
 * to the declaration contexts in JLS 9.6.4.1.
 *
 * <p>For example, an annotation whose type is meta-annotated with
 * {@code @Target(ElementType.FIELD)} may only be written as a modifier for a
 * field declaration.
 *
 * <p>The constant {@link #TYPE_USE} corresponds to the 15 type contexts in JLS
 * 4.11, as well as to two declaration contexts: type declarations (including
 * annotation type declarations) and type parameter declarations.
 *
 * <p>For example, an annotation whose type is meta-annotated with
 * {@code @Target(ElementType.TYPE_USE)} may be written on the type of a field
 * (or within the type of the field, if it is a nested, parameterized, or array
 * type), and may also appear as a modifier for, say, a class declaration.
 *
 * <p>The {@code TYPE_USE} constant includes type declarations and type
 * parameter declarations as a convenience for designers of type checkers which
 * give semantics to annotation types. For example, if the annotation type
 * {@code NonNull} is meta-annotated with
 * {@code @Target(ElementType.TYPE_USE)}, then {@code @NonNull}
 * {@code class C {...}} could be treated by a type checker as indicating that
 * all variables of class {@code C} are non-null, while still allowing
 * variables of other classes to be non-null or not non-null based on whether
 * {@code @NonNull} appears at the variable's declaration.
 *
 * @author  Joshua Bloch
 * @since 1.5
 * @jls 9.6.4.1 @Target
 * @jls 4.1 The Kinds of Types and Values
 */

/**
 * 表示注解应用范围的常量，使用方式：@Target(ElementType.XXX)
 * 除了TYPE_PARAMETER和TYPE_USE属于类型注解，其他注解属于声明式注解
 */
public enum ElementType {
    /** Class, interface (including annotation type), or enum declaration */
    // 用于类、接口、注解、枚举类型的声明
    TYPE,

    /** Field declaration (includes enum constants) */
    // 用于字段、枚举常量的声明
    FIELD,

    /** Method declaration */
    // 方法声明
    METHOD,

    /** Formal parameter declaration */
    // 用于形参声明（包括异常参数）
    PARAMETER,

    /** Constructor declaration */
    // 用于构造器的声明
    CONSTRUCTOR,

    /** Local variable declaration */
    // 用于局部变量的声明
    // 但是需要注意的是，java注解在java 8之前不能用于局部变量
    LOCAL_VARIABLE,

    /** Annotation type declaration */
    // 用于注解类型的声明
    ANNOTATION_TYPE,

    /** Package declaration */
    // 用于包声明
    PACKAGE,

    /**
     * Type parameter declaration
     *
     * @since 1.8
     */
    // 类型参数声明
    TYPE_PARAMETER,

    /**
     * Use of a type
     *
     * @since 1.8
     */
    // 用于广义的类型声明（不能用于package）,比TYPE的应用范围更广，可通过AnnotatedType获取
    TYPE_USE
}
