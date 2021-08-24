package com.example.demo.myassert;

import org.springframework.util.Assert;

/**
 * Assert使用
 *
 * @author legend
 * @version 1.0
 * @description
 * @date 2021/8/24
 */
public class AssertTest {

    public static void main(String[] args) {
        String a = null;
        //如果没有进行捕获异常最后一段日志不进行输出
        //Assert.notNull(a, "Bean name must not be null");

        //如果进行捕获异常则全部日志部分都会输出
        try {
            Assert.notNull(a, "Bean name must not be null");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("出现异常。。。进行捕获：" + e.getMessage());
        } finally {
            System.out.println("finally");
        }
        System.out.println("出现异常继续执行");
    }
}
