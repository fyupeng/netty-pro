package com.fyp.netty.dubborpc.provider;

import com.fyp.netty.dubborpc.nertty.NettyServer;

/**
 * @Auther: fyp
 * @Date: 2022/2/22
 * @Description: 服务端启动类
 * @Package: com.fyp.netty.dubborpc.provider
 * @Version: 1.0
 */
public class ServerBootstrap {

    public static void main(String[] args) {

        NettyServer.startServer("127.0.0.1",7000);

    }

}
