package com.example.demo.autopacking;

/**
 * 自动装箱和拆箱
 *
 * @author legend
 * @version 1.0
 * @description
 * @date 2021/8/22
 */
public class AutoPacking {

    public static void main(String[] args) {
        char c = 'a';
        short s = 1;
        float f = 10f;
        double d = 0.1d;
        System.out.println((c*s+f+d));
    }
}
