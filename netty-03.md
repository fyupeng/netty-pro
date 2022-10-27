netty入门03

# 一、google-protobuf

## 1. 编码和解码的基本介绍

- 编写网络应用程序时，因为数据在网络中传输的都是二进制字节码数据，在发送数据时就需要编码，接收数据时就需要解码[示意图]

- `codec`（编解码器）的组成部分有两个：`decoder`（解码器）和 `encoder`（编码器）。`encoder` 负责把业务数据转换成字节码数据，`decoder` 负责把字节码数据转换成业务数据

![image-20220215105043525](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220215105043525.png)

## 2. Netty 本身的编码解码的机制和问题分析

1. `Netty` 自身提供了一些 `codec`(编解码器)
2. `Netty`提供的编码器
   - `StringEncoder`：对字符串数据进行编码。
   - `ObjectEncoder`：对Java对象进行编码。
3. `Netty`提供的解码器
   - `StringDecoder`,对字符串数据进行解码
   - `ObjectDecoder`，对 Java 对象进行解码
4. `Netty`本身自带的`ObjectDecoder`和`ObjectEncoder`可以用来实现`POJO`对象或各种业务对象的编码和解码，底层使用的仍是Java序列化技术,而Java序列化技术本身效率就不高，存在如下问题
   - 无法跨语言
   - 序列化后的体积太大，是二进制编码的5倍多。
   - 序列化性能太低
5. 引出新的解决方案[`Google` 的 `Protobuf`]

## 3. Protobuf

1. `Protobuf` 基本介绍和使用示意图, protoc下载https://developer.aliyun.com/article/710477
2. `Protobuf` 是 `Google` 发布的开源项目，全称 `Google Protocol Buffers`，是一种轻便高效的结构化数据存储格式，可以用于结构化数据串行化，或者说序列化。它很适合做数据存储或 `RPC` [远程过程调用 `remote procedure call` ]数据交换格式。目前很多公司 从`http + json 转向tcp + protobuf`，效率会更高。
3. 参考文档：https://developers.google.com/protocol-buffers/docs/proto 语言指南
4. `Protobuf` 是以 `message` 的方式来管理数据的.
5. 支持跨平台、跨语言，即[客户端和服务器端可以是不同的语言编写的]（支持目前绝大多数语言，例如 `C++`、`C#`、`Java`、`python` 等）
6. 高性能，高可靠性
7. 使用 `protobuf` 编译器能自动生成代码，`Protobuf` 是将类的定义使用 `.proto` 文件进行描述。说明，在 `idea` 中编写 `.proto` 文件时，会自动提示是否下载 `.ptoto` 编写插件.可以让语法高亮。
8. 然后通过 `protoc.exe` 编译器根据 `.proto` 自动生成 `.java` 文件
9. `protobuf` 使用示意图

![image-20220215114239819](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220215114239819.png)

## 4. Protobuf 快速入门实例1

编写程序，使用 `Protobuf` 完成如下功能

1. 客户端可以发送一个 `StudentPoJo` 对象到服务器(通过 `Protobuf` 编码)
2. 服务端能接收 `StudentPoJo` 对象，并显示信息(通过 `Protobuf` 解码)

```xml
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>3.6.1</version>
</dependency>
```

Student.proto

```protobuf
syntax = "proto3"; //版本
option java_outer_classname = "StudentPOJO"; // 生成的外部类名，同时也是文件名
// protobuf 使用message 管理数据
message Student { // 会在 StudentPOJO 外部类生成一个内部类 Student, 它是真正发送的 POJO对象
    int32 id = 1; // Student 类中有一个 属性 名字为 id类型为 int32(protobuf类型) 1 表示属性序号
    string name = 2;
}
```

编译 protoc.exe --java_out=.Student.proto 将生成的 StudentPOJO 放入到项目使用

![image-20220215132406018](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220215132406018.png)

NettyServer

```java
package com.fyp.netty.codec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;

/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: netty服务端
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyServer {
    public static void main(String[] args) throws InterruptedException {

        // 创建 BossGroup 和 WorkGroup
        /*
            说明：
            1. 创建两个线程组 bossGroup 和 workGroup
            2. bossGroup 只是 处理 连接请求， 真正的 和客户端 业务处理， 会交给 workGroup 来完成
            3. 两个都是 无限循环
            4. bossGroup 和 workerGroup 含有的 子线程 （NioEventLoop）的个数 默认实际 cpu核数 * 2
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            // 创建 服务器端的 启动对象， 配置参数
            ServerBootstrap bootstrap = new ServerBootstrap();

            // 使用 链式编程 来 进行设置
            bootstrap.group(bossGroup, workerGroup) // 设置两个 线程组
                    .channel(NioServerSocketChannel.class) // 使用 NioSocketChannel 作为 服务器的 通道实现
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置 线程队列 得到 连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置 保持活动 连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 创建一个 通道 测试对象

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 在 pipeline 中加入 ProtobufDecoder
                            // 指定对 哪种对象 进行解码
                            pipeline.addLast("decoder", new ProtobufDecoder(StudentPOJO.Student.getDefaultInstance()));
                            pipeline.addLast(new NettyServerHandler());
                        }
                    }); // 给我们的 workerGroup 的 EventLoop 对应的管道 设置处理器

            System.out.println("... 服务器 is ready...");

            // 绑定一个 端口， 并且同步，生成 一个 ChannelFuture 对象
            // 启动 服务器并绑定端口
            ChannelFuture cf = bootstrap.bind(6667).sync();

            // 对 关闭通道 进行监听
            cf.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
```

NettyServerHandler

```java
package com.fyp.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;


/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: nettty服务端处理器
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<StudentPOJO.Student> {

    /**
     * 读取数据
     * @param ctx 上下文对象，含有 管道 pipeline, 通道 channel, 地址
     * @param msg 客户端发送的 数据， 默认类型 Object
     * @throws Exception
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, StudentPOJO.Student msg) throws Exception {

        System.out.println("客户端发送的数据 id " + msg.getId() + " name " + msg.getName());
    }

    // 处理读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);

        // 将数据 写入到 缓存 并 刷新
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端: 狗", CharsetUtil.UTF_8));
    }

    // 处理异常， 要关闭 通道


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

        ctx.close();
    }

}
```

NerttyClient

```java
package com.fyp.netty.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;


/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: netty客户端
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {

        // 客户端 需要 一个 事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {

            // 创建 客户端 启动对象
            // 注意客户端 使用的不是 ServerBootStrap, 而是BootStrap
            Bootstrap bootstrap = new Bootstrap();

            // 设置相关 参数
            bootstrap.group(group) // 设置 线程组
                    .channel(NioSocketChannel.class) // 设置 客户端 通道的 实现类（反射)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 在 pipeline 中 加入 ProtobufEncoder
                            ch.pipeline().addLast("encoder", new ProtobufEncoder());
                            ch.pipeline().addLast(new NettyClientHandler()); // 加入自己的 处理器
                        }
                    });

            System.out.println("客户端 ok...");

            // 启动客户端 去连接 服务器端
            // 关于 ChannelFuture 要分析， 涉及到 netty 的异步模型
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6667).sync();

            // 对关闭 通道 进行监听
            channelFuture.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }
}
```

NettyClientHandler

```java
package com.fyp.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: netty客户端处理器
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        // 发送一个 Student对象 到服务器
        StudentPOJO.Student student = StudentPOJO.Student.newBuilder().setId(4).setName("豹子头 林冲").build();

        ctx.writeAndFlush(student);

    }

    // 当通道 有读取事件 时 ，会触发
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        System.out.println("服务器回复的消息:" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("服务器的地址： "+ ctx.channel().remoteAddress());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        cause.printStackTrace();

        ctx.close();
    }
}
```

## 5. Protobuf 快速入门实例 2

1. 编写程序，使用 `Protobuf` 完成如下功能
2. 客户端可以随机发送 `StudentPoJo` / `WorkerPoJo` 对象到服务器(通过 `Protobuf` 编码)
3. 服务端能接收 `StudentPoJo` / `WorkerPoJo` 对象(需要判断是哪种类型)，并显示信息(通过 `Protobuf` 解码)

```xml
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>3.6.1</version>
</dependency>
```

Student.proto

```protobuf
syntax = "proto3";
option optimize_for = SPEED; //加快解析
option java_package = "com.fyp.netty.codec2"; // 指定生成到哪个包
option java_outer_classname = "MyDataInfo"; // 外部类名

// protobuf 可以使用 message 管理其他的message
message  MyMessage {
    enum DataType {
        StudentType = 0; // 在 proto3 要求 enum 的编号 从0 开始
        WorkerType = 1;
    }
    //用 data_type 来标识 传的是 哪一个 枚举类型
    DataType data_type = 1;

    // 表示 每次 枚举类型 最多 只能出现 其中一个，节省空间
    oneof  dataBody {
        Student student = 2;
        Worker worker = 3;
    }
}

message Student {
    int32 id = 1;
    string name = 2;
}

message Worker {
    string name = 1;
    int32 age = 2;
}
```

NettyServer

```java
package com.fyp.netty.codec2;

import com.fyp.netty.codec.StudentPOJO;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;

/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: netty服务端
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyServer {
    public static void main(String[] args) throws InterruptedException {

        // 创建 BossGroup 和 WorkGroup
        /*
            说明：
            1. 创建两个线程组 bossGroup 和 workGroup
            2. bossGroup 只是 处理 连接请求， 真正的 和客户端 业务处理， 会交给 workGroup 来完成
            3. 两个都是 无限循环
            4. bossGroup 和 workerGroup 含有的 子线程 （NioEventLoop）的个数 默认实际 cpu核数 * 2
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            // 创建 服务器端的 启动对象， 配置参数
            ServerBootstrap bootstrap = new ServerBootstrap();

            // 使用 链式编程 来 进行设置
            bootstrap.group(bossGroup, workerGroup) // 设置两个 线程组
                    .channel(NioServerSocketChannel.class) // 使用 NioSocketChannel 作为 服务器的 通道实现
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置 线程队列 得到 连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置 保持活动 连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 创建一个 通道 测试对象

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 在 pipeline 中加入 ProtobufDecoder
                            // 指定对 哪种对象 进行解码
                            pipeline.addLast("decoder", new ProtobufDecoder(MyDataInfo.MyMessage.getDefaultInstance()));
                            pipeline.addLast(new NettyServerHandler());
                        }
                    }); // 给我们的 workerGroup 的 EventLoop 对应的管道 设置处理器

            System.out.println("... 服务器 is ready...");

            // 绑定一个 端口， 并且同步，生成 一个 ChannelFuture 对象
            // 启动 服务器并绑定端口
            ChannelFuture cf = bootstrap.bind(6667).sync();

            // 对 关闭通道 进行监听
            cf.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
```

NettyServerHandler

```java
package com.fyp.netty.codec2;

import com.fyp.netty.codec.StudentPOJO;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;


/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: nettty服务端处理器
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<MyDataInfo.MyMessage> {

    /**
     * 读取数据
     * @param ctx 上下文对象，含有 管道 pipeline, 通道 channel, 地址
     * @param msg 客户端发送的 数据， 默认类型 Object
     * @throws Exception
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, MyDataInfo.MyMessage msg) throws Exception {

        // 根据 dataType 来显示 不同的信息
        MyDataInfo.MyMessage.DataType dataType = msg.getDataType();

        if (dataType == MyDataInfo.MyMessage.DataType.StudentType) {
            MyDataInfo.Student student = msg.getStudent();
            System.out.println("学生id " + student.getId() + "学生名字 " + student.getName());
        } else if (dataType == MyDataInfo.MyMessage.DataType.WorkerType) {
            MyDataInfo.Worker worker = msg.getWorker();
            System.out.println("工人名字" + worker.getName() + "工人年龄" + worker.getAge());
        } else {
            System.out.println("传输的类型不正确");
        }

    }

    // 处理读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);

        // 将数据 写入到 缓存 并 刷新
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端: 狗", CharsetUtil.UTF_8));
    }

    // 处理异常， 要关闭 通道


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

        ctx.close();
    }

}
```

NettyClient

```java
package com.fyp.netty.codec2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;


/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: netty客户端
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {

        // 客户端 需要 一个 事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {

            // 创建 客户端 启动对象
            // 注意客户端 使用的不是 ServerBootStrap, 而是BootStrap
            Bootstrap bootstrap = new Bootstrap();

            // 设置相关 参数
            bootstrap.group(group) // 设置 线程组
                    .channel(NioSocketChannel.class) // 设置 客户端 通道的 实现类（反射)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 在 pipeline 中 加入 ProtobufEncoder
                            ch.pipeline().addLast("encoder", new ProtobufEncoder());
                            ch.pipeline().addLast(new NettyClientHandler()); // 加入自己的 处理器
                        }
                    });

            System.out.println("客户端 ok...");

            // 启动客户端 去连接 服务器端
            // 关于 ChannelFuture 要分析， 涉及到 netty 的异步模型
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6667).sync();

            // 对关闭 通道 进行监听
            channelFuture.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }
}
```

NettyClientHandler

```java
package com.fyp.netty.codec2;

import com.fyp.netty.codec.StudentPOJO;
import com.fyp.netty.heartbeat.MyServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.Random;

/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: netty客户端处理器
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        // 随机的 发送 Student 或者 Worker 对象
        int random = new Random().nextInt(3);
        MyDataInfo.MyMessage myMessage = null;

        if (0 == random) { // 发送 student 对象
            myMessage = MyDataInfo.MyMessage.newBuilder().setDataType(MyDataInfo.MyMessage.DataType.StudentType).setStudent(MyDataInfo.Student.newBuilder().setId(5).setName("玉麒麟 卢俊义").build()).build();
        } else {
            myMessage = MyDataInfo.MyMessage.newBuilder().setDataType(MyDataInfo.MyMessage.DataType.WorkerType).setWorker(MyDataInfo.Worker.newBuilder().setName("老李").setAge(20).build()).build();
        }
        ctx.writeAndFlush(myMessage);
    }

    // 当通道 有读取事件 时 ，会触发
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        System.out.println("服务器回复的消息:" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("服务器的地址： "+ ctx.channel().remoteAddress());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        cause.printStackTrace();

        ctx.close();
    }
}
```

# 二、Netty出入站和调用机制

## 1. 基本说明

1. `Netty` 的组件设计：`Netty` 的主要组件有 `Channel`、`EventLoop`、`ChannelFuture`、`ChannelHandler`、`ChannelPipe` 等
2. `ChannelHandler` 充当了处理入站和出站数据的应用程序逻辑的容器。例如，实现 `ChannelInboundHandler` 接口（或 `ChannelInboundHandlerAdapter`），你就可以接收入站事件和数据，这些数据会被业务逻辑处理。当要给客户端发送响应时，也可以从 `ChannelInboundHandler` 冲刷数据。业务逻辑通常写在一个或者多个 `ChannelInboundHandler` 中。`ChannelOutboundHandler` 原理一样，只不过它是用来处理出站数据的
3. `ChannelPipeline` 提供了 `ChannelHandler` 链的容器。以客户端应用程序为例，如果事件的运动方向是从客户端到服务端的，那么我们称这些事件为出站的，即客户端发送给服务端的数据会通过 `pipeline` 中的一系列 `ChannelOutboundHandler`，并被这些 `Handler` 处理，反之则称为入站的

![image-20220215204236604](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20220215204236604.png)

## 2. 编码解码器

1. 当 `Netty` 发送或者接受一个消息的时候，就将会发生一次数据转换。入站消息会被解码：从字节转换为另一种格式（比如 `java` 对象）；如果是出站消息，它会被编码成字节。
2. `Netty` 提供一系列实用的编解码器，他们都实现了 `ChannelInboundHadnler` 或者 `ChannelOutboundHandler` 接口。在这些类中，`channelRead` 方法已经被重写了。以入站为例，对于每个从入站 `Channel` 读取的消息，这个方法会被调用。随后，它将调用由解码器所提供的 `decode()` 方法进行解码，并将已经解码的字节转发给 `ChannelPipeline` 中的下一个 `ChannelInboundHandler`。

## 3. 解码器 - ByteToMessageDecoder

1. 关系继承图

![img](https://unpkg.zhimg.com/youthlql@1.0.0/netty/introduction/chapter_003/0005.png)

1. 由于不可能知道远程节点是否会一次性发送一个完整的信息，`tcp` 有可能出现粘包拆包的问题，这个类会对入站数据进行缓冲，直到它准备好被处理.【后面有说TCP的粘包和拆包问题】
2. 一个关于 `ByteToMessageDecoder` 实例分析

![image-20220215210052549](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220215210052549.png)

## 4. Netty的handler链的调用机制

实例要求:

1. 使用自定义的编码器和解码器来说明 `Netty` 的 `handler` 调用机制 

   客户端发送 `long` -> 服务器 

   服务端发送 `long` -> 客户端

![image-20220216203347980](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220216203347980.png)

### 代码演示

MyServer

```java
package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description: 我的服务端
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MyServerInitializer()); // 自定义一个 初始化类

            ChannelFuture channelFuture = serverBootstrap.bind(7000).sync();
            channelFuture.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
```

MyServerHandler

```java
package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description: 我的服务端处理器
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyServerHandler extends SimpleChannelInboundHandler<Long> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Long msg) throws Exception {

        System.out.println("从客户端" + ctx.channel().remoteAddress() + "读取到的long " + msg);

        ctx.writeAndFlush(98765L);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

MyServerInitializer

```java
package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description: 服务器初始化类
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 入站的 handler 进行解码 MyByteToLongDecoder
        pipeline.addLast(new MyByteToLongDecoder());
        // 出站的 handler 进行编码 MyLongToByteEncoder
        pipeline.addLast(new MyLongToByteEncoder());
        // 自定义的 handler 处理业务逻辑
        pipeline.addLast(new MyServerHandler());


    }
}
```

MyClient

```java

package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyClient {
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();

        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new MyClientInitializer()); // 自定义一个 初始化类

            ChannelFuture channelFuture = bootstrap.connect("localhost", 7000).sync();
            channelFuture.channel().closeFuture().sync();


        } finally {
            group.shutdownGracefully();
        }

    }

}

```

MyClientHandler

```java
package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyClientHandler extends SimpleChannelInboundHandler<Long> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Long msg) throws Exception {
        System.out.println("服务器端发回的消息：" + msg);
    }

    // 发送数据
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("MyClientHandler 发送数据");
        //ctx.writeAndFlush(Unpooled.copiedBuffer())
        ctx.writeAndFlush(123456L); // 发送的是 一个 Long
    }
}
```

MyClientInitializer

```java
package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description: 客户端初始化类
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyClientInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();
        // 加入 一个 入站的 handler 对数据 进行 解码
        pipeline.addLast(new MyByteToLongDecoder());
        // 加入 一个 出站的 handler 对数据 进行 编码
        pipeline.addLast(new MyLongToByteEncoder());
        // 加入 一个自定义的 handler 处理业务
        pipeline.addLast(new MyClientHandler());

    }
}
```

MyByteToLongDecoder

```java
package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyByteToLongDecoder extends ByteToMessageDecoder {

    /**
     * decode 会根据接受的数据，调用多次，直到  确定没有 新的元素 添加到 list，
     *        或者是 ByteBuf  没有更多的 可读字节为止
     *        如果list out 不为空， 就会将 list 的内容 传递给 下一个 ChannelInBoundHandler
     *        处理，该处理器的 方法也会被 调用多次
     * @param ctx 上下文对象
     * @param in 入站的 ByteBuf
     * @param out list 集合，将解码后的 数据 传给下一个 handler
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        System.out.println("MyByteToLongDecoder decoder 被调用");

        // 因为 long 8个字节
         if (in.readableBytes() >= 8) {
             out.add(in.readLong());
         }

    }

}
```

MyLongToByteEncoder

```java
package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyLongToByteEncoder extends MessageToByteEncoder<Long> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Long msg, ByteBuf out) throws Exception {
        System.out.println("MyLongToByteEncoder encode 被调用");
        System.out.println("msg " + msg);
        out.writeLong(msg);
    }


}
```

### 效果

服务端：handler调用顺序：MyByteToLongDecoder -> MyServerHandler-> MyLongToByteEncoder

![image-20220216213708999](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220216213708999.png)



客户端：handler调用顺序：MyClientHandler-> MyLongToByteEncoder -> MyByteToLongDecoder 

![image-20220216225608673](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220216225608673.png)

### 出站入站

关于出站入站，很多人可能有点迷糊 1）客户端有出站入站，服务端也有出站入站 2）以客户端为例，如果有服务端传送的数据到达客户端，那么对于客户端来说就是入站； 如果客户端传送数据到服务端，那么对于客户端来说就是出站； 同理，对于服务端来说，也是一样的，有数据来就是入站，有数据输出就是出站 3）为什么服务端和客户端的Serverhandler都是继承`SimpleChannelInboundHandler`，而没有`ChannelOutboundHandler`出站类？ 实际上当我们在handler中调用ctx.writeAndFlush()方法后，就会将数据交给ChannelOutboundHandler进行出站处理，只是我们没有去定义出站类而已，若有需求可以自己去实现ChannelOutboundHandler出站类 4）总结就是客户端和服务端都有出站和入站的操作 **服务端发数据给客户端：**服务端--->出站--->Socket通道--->入站--->客户端

 **客户端发数据给服务端：**客户端--->出站--->Socket通道--->入站--->服务端

### 理透handler链执行入站出站原理（双链结构）

理解：通过对继承的类来过滤是出站的还是入站的，然后再去执行该类的实现方法

head ↔ ServerInitializer(服务端初始化器) ↔ decoder(解码器) ↔ encoder(编码器) ↔ ServerHandler(自定义处理器)↔ tail

通过以下的代码：

过滤后得到：

入站(head->tail方向)：decoder(解码器) -> ServerHandler(自定义处理器)

出站(tail->head方向)：ServerHandler(自定义处理器) -> encoder(编码器)

```java
private AbstractChannelHandlerContext findContextInbound(int mask) {
        AbstractChannelHandlerContext ctx = this;
        EventExecutor currentExecutor = executor();
        do {
            ctx = ctx.next;
        } while (skipContext(ctx, currentExecutor, mask, MASK_ONLY_INBOUND));
        return ctx;
}

private AbstractChannelHandlerContext findContextOutbound(int mask) {
        AbstractChannelHandlerContext ctx = this;
        EventExecutor currentExecutor = executor();
        do {
            ctx = ctx.prev;
        // 会跳过只处理那些入站的 handler
        } while (skipContext(ctx, currentExecutor, mask, MASK_ONLY_OUTBOUND));
        return ctx;
}
```

## 5. ByteToMessageDecoder

- 不论解码器 `handler` 还是编码器 `handler` 即接收的消息类型必须与待处理的消息类型一致，否则该 `handler` 不会被执行
- 在解码器进行数据解码时，需要判断缓存区（`ByteBuf`）的数据是否足够，否则接收到的结果会期望结果可能不一致。

## 6. 解码器 - ReplayingDecoder

1. `public abstract class ReplayingDecoder<S> extends ByteToMessageDecoder`
2. `ReplayingDecoder` 扩展了 `ByteToMessageDecoder` 类，使用这个类，我们不必调用 `readableBytes()` 方法，也就不用判断还有没有足够的数据来读取。参数 `S` 指定了用户状态管理的类型，其中 `Void` 代表不需要状态管理
3. 应用实例：使用 `ReplayingDecoder` 编写解码器，对前面的案例进行简化[案例演示]

```java
package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyByteToLongDecoder2 extends ReplayingDecoder<Void> {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("MyByteToLongDecoder2 被调用");
        // 在 ReplayingDecoder 不需要 判断 数据 是否足够 读取，内部会进行 处理判断
        out.add(in.readLong());
    }
}
```

1. `ReplayingDecoder`使用方便，但它也有一些局限性：
   - 并不是所有的 `ByteBuf` 操作都被支持，如果调用了一个不被支持的方法，将会抛出一个 `UnsupportedOperationException`。
   - `ReplayingDecoder` 在某些情况下可能稍慢于 `ByteToMessageDecoder`，例如网络缓慢并且消息格式复杂时，消息会被拆成了多个碎片，速度变慢

## 7. 其它编解码器

1. `LineBasedFrameDecoder`：这个类在 `Netty` 内部也有使用，它使用行尾控制字符（\n或者\r\n）作为分隔符来解析数据。
2. `DelimiterBasedFrameDecoder`：使用自定义的特殊字符作为消息的分隔符。
3. `HttpObjectDecoder`：一个 `HTTP` 数据的解码器
4. `LengthFieldBasedFrameDecoder`：通过指定长度来标识整包消息，这样就可以自动的处理黏包和半包消息。

## 8. Log4j 整合到 Netty

1. 在 `Maven` 中添加对 `Log4j` 的依赖在 `pom.xml`

```xml
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.25</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-log4j12</artifactId>
    <version>1.7.25</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.25</version>
    <scope>test</scope>
</dependency>
```

1. 配置 `Log4j`，在 `resources/log4j.properties`

```properties
log4j.rootLogger=DEBUG,stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=[%p]%C{1}-%m%n
```

# 三、TCP粘包和拆包	

## 1. TCP 粘包和拆包基本介绍

1. `TCP` 是面向连接的，面向流的，提供高可靠性服务。收发两端（客户端和服务器端）都要有一一成对的 `socket`，因此，发送端为了将多个发给接收端的包，更有效的发给对方，使用了优化方法（`Nagle` 算法），将多次间隔较小且数据量小的数据，合并成一个大的数据块，然后进行封包。这样做虽然提高了效率，但是接收端就难于分辨出完整的数据包了，因为面向流的通信是无消息保护边界的
2. 由于 `TCP` 无消息保护边界,需要在接收端处理消息边界问题，也就是我们所说的粘包、拆包问题,看一张图
3. `TCP` 粘包、拆包图解

![image-20220217154943900](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220217154943900.png)

假设客户端分别发送了两个数据包 `D1` 和 `D2` 给服务端，由于服务端一次读取到字节数是不确定的，故可能存在以下四种情况：

1. 服务端分两次读取到了两个独立的数据包，分别是 `D1` 和 `D2`，没有粘包和拆包
2. 服务端一次接受到了两个数据包，`D1` 和 `D2` 粘合在一起，称之为 `TCP` 粘包
3. 服务端分两次读取到了数据包，第一次读取到了完整的 `D1` 包和 `D2` 包的部分内容，第二次读取到了 `D2` 包的剩余内容，这称之为 `TCP` 拆包
4. 服务端分两次读取到了数据包，第一次读取到了 `D1` 包的部分内容 `D1_1`，第二次读取到了 `D1` 包的剩余部分内容 `D1_2` 和完整的 `D2` 包。

## 2. TCP 粘包和拆包现象实例

在编写 `Netty` 程序时，如果没有做处理，就会发生粘包和拆包的问题

看一个具体的实例：

MyServer

```java
package com.fyp.netty.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description: 我的服务端
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MyServerInitializer()); // 自定义一个 初始化类

            ChannelFuture channelFuture = serverBootstrap.bind(7000).sync();
            channelFuture.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
```

MyServerInitializer

```java
package com.fyp.netty.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.tcp
 * @Version: 1.0
 */
public class MyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MyServerHandler());
    }
}
```

MyServerHandler

```java
package com.fyp.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.tcp
 * @Version: 1.0
 */
public class MyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte[] buffer = new byte[msg.readableBytes()];
        msg.readBytes(buffer);

        // 将 buffer 转成 字符串
        String message = new String(buffer, Charset.forName("UTF-8"));

        System.out.println("服务器接收到数据 " + message);
        System.out.println("服务器接收到数据量= " + (++this.count));

        // 服务端 回送数据 给客户端 ，回送一个 随机 id
        ByteBuf responseByteBuf = Unpooled.copiedBuffer(UUID.randomUUID().toString() + "\n", Charset.forName("utf-8"));
        ctx.writeAndFlush(responseByteBuf);
    }
}
```

MyClient

```java
package com.fyp.netty.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyClient {
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();

        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new MyClientInitializer()); // 自定义一个 初始化类

            ChannelFuture channelFuture = bootstrap.connect("localhost", 7000).sync();
            channelFuture.channel().closeFuture().sync();


        } finally {
            group.shutdownGracefully();
        }

    }

}
```

MyClientInitializer

```java
package com.fyp.netty.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.tcp
 * @Version: 1.0
 */
public class MyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new MyClientHandler());
    }
}
```

MyClientHandler

```java
package com.fyp.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.tcp
 * @Version: 1.0
 */
public class MyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private int count;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        // 使用 客户端 发送 十条 数据，从而模拟 tcp 的 粘包和拆包
        for (int i = 0; i <10; i++) {
            ByteBuf buffer = Unpooled.copiedBuffer("hello,server" + i, Charset.forName("utf-8"));
            ctx.writeAndFlush(buffer);
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        byte[] buffer = new byte[msg.readableBytes()];
        msg.readBytes(buffer);

        String message = new String(buffer, Charset.forName("utf-8"));
        System.out.println("客户端接收到消息=" + message);
        System.out.println("客户端接受消息数据= " + (++this.count));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

## 3. TCP 粘包和拆包解决方案

1. 常用方案：使用自定义协议+编解码器来解决
2. 关键就是要解决服务器端每次读取数据长度的问题，这个问题解决，就不会出现服务器多读或少读数据的问题，从而避免的 `TCP` 粘包、拆包。

**看一个具体的实例**

1. 要求客户端发送 `5` 个 `Message` 对象，客户端每次发送一个 `Message` 对象
2. 服务器端每次接收一个 `Message`，分 `5` 次进行解码，每读取到一个 `Message`，会回复一个 `Message` 对象给客户端。

![image-20220217163246417](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220217163246417.png)

MessageProtoco

```java
package com.fyp.netty.protocoltcp;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description: 协议包
 * @Package: com.fyp.netty.protocoltcp
 * @Version: 1.0
 */
public class MessageProtocol {

    private int len;
    private byte[] content;

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
```

MyServer

```java
package com.fyp.netty.protocoltcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description: 我的服务端
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MyServerInitializer()); // 自定义一个 初始化类

            ChannelFuture channelFuture = serverBootstrap.bind(7000).sync();
            channelFuture.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
```

MyServerInitializer

```java
package com.fyp.netty.protocoltcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.tcp
 * @Version: 1.0
 */
public class MyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new MyMessageDecoder());
        pipeline.addLast(new MyMessageEncoder());
        pipeline.addLast(new MyServerHandler());
    }
}
```

MyServerHandler

```java
package com.fyp.netty.protocoltcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.tcp
 * @Version: 1.0
 */
public class MyServerHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws Exception {
        //接收到数据，并处理
        int len = msg.getLen();
        byte[] content = msg.getContent();

        System.out.println("服务器接收到信息如下");
        System.out.println("长度=" + len);
        System.out.println("内容=" + new String(content, Charset.forName("utf-8")));

        System.out.println("服务器接收到消息包数量=" + (++this.count));

        //回复消息
        System.out.println("服务端开始回复消息------");
        String responseContent = UUID.randomUUID().toString();
        int responseLen = responseContent.getBytes("utf-8").length;
        byte[]  responseContent2 = responseContent.getBytes("utf-8");
        //构建一个协议包
        MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol.setLen(responseLen);
        messageProtocol.setContent(responseContent2);

        ctx.writeAndFlush(messageProtocol);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //cause.printStackTrace();
        ctx.close();
    }

}
```

MyMessageDecoder

```java
package com.fyp.netty.protocoltcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.protocoltcp
 * @Version: 1.0
 */
public class MyMessageDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("MyMessageDecoder decode 被调用");
        // 需要 将 二进制 字节码 -> MessageProtocol 数据包（对象）
        /**
         * 获取 是 在 之前的 编码器写入的 int 和 byte 类型数据
         *      out.writeInt(msg.getLen());
     *          out.writeBytes(msg.getContent());
         */
        int length = in.readInt();


        byte[] content = new byte[length];
        in.readBytes(content);

        MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol.setLen(length);
        messageProtocol.setContent(content);

        out.add(messageProtocol);
    }
}
```

MyClient

```java
package com.fyp.netty.protocoltcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyClient {
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();

        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new MyClientInitializer()); // 自定义一个 初始化类

            ChannelFuture channelFuture = bootstrap.connect("localhost", 7000).sync();
            channelFuture.channel().closeFuture().sync();


        } finally {
            group.shutdownGracefully();
        }

    }

}
```

MyClientInitializer

```java
package com.fyp.netty.protocoltcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.tcp
 * @Version: 1.0
 */
public class MyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new MyMessageEncoder());
        pipeline.addLast(new MyMessageDecoder());
        pipeline.addLast(new MyClientHandler());
    }

}
```

MyClientHandler

```java
package com.fyp.netty.protocoltcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.tcp
 * @Version: 1.0
 */
public class MyClientHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private int count;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {


        // 使用客户端 发送十条数据，今天天气冷，吃火锅
        for (int i = 0; i < 5; i++) {
            String mes = "abc";
            byte[] content = mes.getBytes(Charset.forName("utf-8"));
            int length = content.length;

            // 创建 协议包 对象
            MessageProtocol messageProtocol = new MessageProtocol();
            messageProtocol.setLen(length);
            messageProtocol.setContent(content);

            ctx.writeAndFlush(messageProtocol);

        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol msg) throws Exception {
        int len = msg.getLen();
        byte[] content = msg.getContent();

        System.out.println("客户端接收到消息如下");
        System.out.println("长度=" + len);
        System.out.println("内容=" + new String(content, Charset.forName("utf-8")));

        System.out.println("客户端接收消息数量=" + (++this.count));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常提醒=" + cause.getMessage());
        ctx.close();
    }
}
```

MyMessageEncoder

```java
package com.fyp.netty.protocoltcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.protocoltcp
 * @Version: 1.0
 */
public class MyMessageEncoder extends MessageToByteEncoder<MessageProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProtocol msg, ByteBuf out) throws Exception {
        System.out.println("MyMessageEncoder encode 方法 被调用");
        out.writeInt(msg.getLen());
        out.writeBytes(msg.getContent());
    }

}
```

# 四、Netty实现RPC

## 1. RPC 基本介绍

1. `RPC（Remote Procedure Call）`—远程过程调用，是一个计算机通信协议。该协议允许运行于一台计算机的程序调用另一台计算机的子程序，而程序员无需额外地为这个交互作用编程
2. 两个或多个应用程序都分布在不同的服务器上，它们之间的调用都像是本地方法调用一样(如图)

![image-20220221233101397](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220221233101397.png)

过程：

1. 调用者(`Caller`)，调用远程API(`Remote API`)
2. 调用远程API会通过一个RPC代理(`RpcProxy`)
3. RPC代理再去调用`RpcInvoker`(这个是PRC的调用者)
4. `RpcInvoker`通过RPC连接器(`RpcConnector`)
5. RPC连接器用两台机器规定好的PRC协议(`RpcProtocol`)把数据进行编码
6. 接着RPC连接器通过RpcChannel通道发送到对方的PRC接收器(RpcAcceptor)
7. PRC接收器通过PRC协议进行解码拿到数据
8. 然后将数据传给`RpcProcessor`
9. `RpcProcessor`再传给`RpcInvoker`
10. `RpcInvoker`调用`Remote API`
11. 最后推给被调用者(Callee)

3. 常见的 `RPC` 框架有：比较知名的如阿里的 `Dubbo`、`Google` 的 `gRPC`、`Go` 语言的 `rpcx`、`Apache` 的 `thrift`，`Spring` 旗下的 `SpringCloud`。

![](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220221233101397.png)

## 2. 我们的RPC 调用流程图

![image-20220221233329677](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220221233329677.png)

**RPC 调用流程说明**

1. 服务消费方（`client`）以本地调用方式调用服务
2. `client stub` 接收到调用后负责将方法、参数等封装成能够进行网络传输的消息体
3. `client stub` 将消息进行编码并发送到服务端
4. `server stub` 收到消息后进行解码
5. `server stub` 根据解码结果调用本地的服务
6. 本地服务执行并将结果返回给 `server stub`
7. `server stub` 将返回导入结果进行编码并发送至消费方
8. `client stub` 接收到消息并进行解码
9. 服务消费方（`client`）得到结果

小结：`RPC` 的目标就是将 `2 - 8` 这些步骤都封装起来，用户无需关心这些细节，可以像调用本地方法一样即可完成远程服务调用

## 3. 己实现 Dubbo RPC（基于 Netty）

### 需求说明

1. `Dubbo` 底层使用了 `Netty` 作为网络通讯框架，要求用 `Netty` 实现一个简单的 `RPC` 框架
2. 模仿 `Dubbo`，消费者和提供者约定接口和协议，消费者远程调用提供者的服务，提供者返回一个字符串，消费者打印提供者返回的数据。底层网络通信使用 `Netty 4.1.20`

### 设计说明

1. 创建一个接口，定义抽象方法。用于消费者和提供者之间的约定。
2. 创建一个提供者，该类需要监听消费者的请求，并按照约定返回数据。
3. 创建一个消费者，该类需要透明的调用自己不存在的方法，内部需要使用 `Netty` 请求提供者返回数据
4. 开发的分析图

![image-20220221233852469](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220221233852469.png)

使用rpc框架打包时，如果是用的是全局库，打包会找不到netty的jar包，需要通过maven引入的方式打包