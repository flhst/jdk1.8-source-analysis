package tests.java.lang;

import org.junit.Test;

/**
 * Object class tests
 * 04 ObjectԴ�����   Object���еķ������ã��Ա�����
 *
 * @author raysonfang
 * @date 2019-8-8
 */
public class ObjectTest {

    public static void main(String[] args) {

    }

    /**
     * Object#getClass()
     */
    @Test
    public void testGetClass(){

        /**
         * �˷����ǲ���getClass()��.class ��getSuperclass()���ߵ�����
         * ���н��Ϊ��
         * ��ǰ������Ϊ:class tests.java.lang.Son
         * ͨ��class���Ի�ȡ��������class tests.java.lang.Parent
         * ��ǰ������ļ̳еĸ���Ϊ��class tests.java.lang.Parent
         */
        Son son = new Son();
        System.out.println("��ǰ������Ϊ:" + son.getClass());
        System.out.println("ͨ��class���Ի�ȡ��������" + Parent.class);
        System.out.println("��ǰ������ļ̳еĸ���Ϊ��" + son.getClass().getSuperclass());
    }
}