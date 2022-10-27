package com.fyp.netty.dubborpc.provider;

import com.fyp.netty.dubborpc.publiciniterface.HelloService;

/**
 * @Auther: fyp
 * @Date: 2022/2/22
 * @Description: 服务提供方的真实实现
 * @Package: com.fyp.netty.dubborpc.provider
 * @Version: 1.0
 */
public class HelloServiceImpl implements HelloService {

    /**
     *
     * @param mes
     * @return 返回一个字符串
     */
    @Override
    public String hello(String mes) {
        System.out.println("收到客户端消息=" + mes);
        if (mes != null) {
            return "你好客户端，我已经收到你的消息 [ " + mes + "]";
        }
        return "你好客户端，我已经收到你的消息";
    }




}
