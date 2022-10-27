package com.fyp.netty.groupchat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description: 测试类
 * @Package: com.fyp.netty.groupchat
 * @Version: 1.0
 */
public class Test {

    public static void main(String[] args) {

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);




        list.forEach(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                System.out.println(o + " a ");
            }
        });
    }

}
