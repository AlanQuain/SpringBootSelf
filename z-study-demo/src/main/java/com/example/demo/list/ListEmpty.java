package com.example.demo.list;

import org.apache.commons.collections4.ListUtils;
import org.assertj.core.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 空集合
 *
 * @author legend
 * @version 1.0
 * @description
 * @date 2021/8/24
 */
public class ListEmpty {

    public static void main(String[] args) {
        ArrayList<Object> list1 = Lists.newArrayList();
        List<Object> list2 = null;
        List<Object> list3 = ListUtils.emptyIfNull(list2);
        List<Object> list4 = Collections.emptyList();
        System.out.println(list3.size());
        //空指针
        System.out.println(list2.size());
    }
}
