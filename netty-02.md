Netty入门02

# 一、Netty 概述

## 1. 原生 NIO 存在的问题

1. `NIO` 的类库和 `API` 繁杂，使用麻烦：需要熟练掌握 `Selector`、`ServerSocketChannel`、`SocketChannel`、`ByteBuffer`等。
2. 需要具备其他的额外技能：要熟悉 `Java` 多线程编程，因为 `NIO` 编程涉及到 `Reactor` 模式，你必须对多线程和网络编程非常熟悉，才能编写出高质量的 `NIO` 程序。
3. 开发工作量和难度都非常大：例如客户端面临断连重连、网络闪断、半包读写、失败缓存、网络拥塞和异常流的处理等等。
4. `JDK NIO` 的 `Bug`：例如臭名昭著的 `Epoll Bug`，它会导致 `Selector` 空轮询，最终导致 `CPU100%`。直到 `JDK1.7` 版本该问题仍旧存在，没有被根本解决。

## 2. Netty 官网说明

官网：https://netty.io/

Netty is an asynchronous event-driven network application framework for rapid development of maintainable high performance protocol servers & clients.

![image-20220209162244928](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220209162244928.png)

## 3. Netty 的优点

`Netty` 对 `JDK` 自带的 `NIO` 的 `API` 进行了封装，解决了上述问题。

1. 设计优雅：适用于各种传输类型的统一 `API` 阻塞和非阻塞 `Socket`；基于灵活且可扩展的事件模型，可以清晰地分离关注点；高度可定制的线程模型-单线程，一个或多个线程池。
2. 使用方便：详细记录的 `Javadoc`，用户指南和示例；没有其他依赖项，`JDK5（Netty3.x）`或 `6（Netty4.x）`就足够了。
3. 高性能、吞吐量更高：延迟更低；减少资源消耗；最小化不必要的内存复制。
4. 安全：完整的 `SSL/TLS` 和 `StartTLS` 支持。
5. 社区活跃、不断更新：社区活跃，版本迭代周期短，发现的 `Bug` 可以被及时修复，同时，更多的新功能会被加入。

## 4. Netty 版本说明

1. `Netty` 版本分为 `Netty 3.x` 和 `Netty 4.x`、`Netty 5.x`
2. 因为 `Netty 5` 出现重大 `bug`，已经被官网废弃了，目前推荐使用的是 `Netty 4.x`的稳定版本
3. 目前在官网可下载的版本 `Netty 3.x`、`Netty 4.0.x` 和 `Netty 4.1.x`
4. 在本套课程中，我们讲解 `Netty4.1.x` 版本
5. `Netty` 下载地址：https://bintray.com/netty/downloads/netty/

## 5. Netty Maven 下载

### 5.1 通过官网下载

点击进入 `Project Structure`

点击`Library`

添加`Library From Maven`

输入 `io.netty:netty-all:4.1.20.Final` 后查找

### 5.2 通过 maven

```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>${netty.version}</version>
</dependency>
```

# 二、Netty 高性能架构设计

## 1. 线程模型基本介绍

1. 不同的线程模式，对程序的性能有很大影响，为了搞清 `Netty` 线程模式，我们来系统的讲解下各个线程模式，最后看看 `Netty` 线程模型有什么优越性。

2. 目前存在的线程模型有：传统阻塞 `I/O` 服务模型 和`Reactor` 模式

3. 根据`Reactor`的数量和处理资源池线程的数量不同，有`3`种典型的实现
   - 单 `Reactor` 单线程；
   - 单 `Reactor`多线程；
   - 主从 `Reactor`多线程

4. `Netty` 线程模式（`Netty` 主要基于主从 `Reactor` 多线程模型做了一定的改进，其中主从 `Reactor` 多线程模型有多个 `Reactor`）

## 2. 传统阻塞 I/O 服务模型

### 工作原理图

1. 黄色的框表示对象，蓝色的框表示线程
2. 白色的框表示方法（`API`）

### 模型特点

1. 采用阻塞 `IO` 模式获取输入的数据
2. 每个连接都需要独立的线程完成数据的输入，业务处理，数据返回

### 问题分析

1. 当并发数很大，就会创建大量的线程，占用很大系统资源
2. 连接创建后，如果当前线程暂时没有数据可读，该线程会阻塞在 Handler对象中的`read` 操作，导致上面的处理线程资源浪费

![image-20220209164518093](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220209164518093.png)

## 3. Reactor 模式

### 针对传统阻塞 I/O 服务模型的 2 个缺点，解决方案：

基于 `I/O` 复用模型：多个连接共用一个阻塞对象`ServiceHandler`，应用程序只需要在一个阻塞对象等待，无需阻塞等待所有连接。当某个连接有新的数据可以处理时，操作系统通知应用程序，线程从阻塞状态返回，开始进行业务处理。

`Reactor` 在不同书中的叫法：

1. 反应器模式
2. 分发者模式（Dispatcher）
3. 通知者模式（notifier）
4. 基于线程池复用线程资源：不必再为每个连接创建线程，将连接完成后的业务处理任务分配给线程进行处理，一个线程可以处理多个连接的业务。（解决了当并发数很大时，会创建大量线程，占用很大系统资源）
5. 基于 `I/O` 复用模型：多个客户端进行连接，先把连接请求给`ServiceHandler`。多个连接共用一个阻塞对象`ServiceHandler`。假设，当C1连接没有数据要处理时，C1客户端只需要阻塞于`ServiceHandler`，C1之前的处理线程便可以处理其他有数据的连接，不会造成线程资源的浪费。当C1连接再次有数据时，`ServiceHandler`根据线程池的空闲状态，将请求分发给空闲的线程来处理C1连接的任务。（解决了线程资源浪费的那个问题）

![image-20220210110506049](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220210110506049.png)

### I/O 复用结合线程池，就是 Reactor 模式基本设计思想，如图![](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220210110734212.png)

对上图说明：

1. `Reactor` 模式，通过一个或多个输入同时传递给服务处理器（ServiceHandler）的模式（基于事件驱动）
2. 服务器端程序处理传入的多个请求,并将它们同步分派到相应的处理线程，因此 `Reactor` 模式也叫 `Dispatcher` 模式
3. `Reactor` 模式使用 `IO` 复用监听事件，收到事件后，分发给某个线程（进程），这点就是网络服务器高并发处理关键

>  原先有多个Handler阻塞，现在只用一个ServiceHandler阻塞

### Reactor 模式中核心组成

1. `Reactor（也就是那个ServiceHandler）`：`Reactor` 在一个单独的线程中运行，负责监听和分发事件，分发给适当的处理线程来对 `IO` 事件做出反应。它就像公司的电话接线员，它接听来自客户的电话并将线路转移到适当的联系人；
2. `Handlers（处理线程EventHandler）`：处理线程执行 `I/O` 事件要完成的实际事件，类似于客户想要与之交谈的公司中的实际官员。`Reactor` 通过调度适当的处理线程来响应 `I/O` 事件，处理程序执行非阻塞操作。

### Reactor 模式分类

根据 `Reactor` 的数量和处理资源池线程的数量不同，有 `3` 种典型的实现

1. 单 `Reactor` 单线程
2. 单 `Reactor` 多线程
3. 主从 `Reactor` 多线程

## 4. 单 Reactor 单线程

原理图，并使用 `NIO` 群聊系统验证

![image-20220210110920005](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220210110920005.png)

### 方案说明

1. `Select` 是前面 `I/O` 复用模型介绍的标准网络编程 `API`，可以实现应用程序通过一个阻塞对象监听多路连接请求
2. `Reactor` 对象通过 `Select` 监控客户端请求事件，收到事件后通过 `Dispatch` 进行分发
3. 如果是建立连接请求事件，则由 `Acceptor` 通过 `Accept` 处理连接请求，然后创建一个 `Handler` 对象处理连接完成后的后续业务处理
4. 如果不是建立连接事件，则 `Reactor` 会分发调用连接对应的 `Handler` 来响应
5. `Handler` 会完成 `Read` → 业务处理 → `Send` 的完整业务流程

结合实例：服务器端用一个线程通过多路复用搞定所有的 `IO` 操作（包括连接，读、写等），编码简单，清晰明了，但是如果客户端连接数量较多，将无法支撑，前面的 `NIO` 案例就属于这种模型。

### 方案优缺点分析

1. 优点：模型简单，没有多线程、进程通信、竞争的问题，全部都在一个线程中完成
2. 缺点：性能问题，只有一个线程，无法完全发挥多核 `CPU` 的性能。`Handler`在处理某个连接上的业务时，整个进程无法处理其他连接事件，很容易导致性能瓶颈
3. 缺点：可靠性问题，线程意外终止，或者进入死循环，会导致整个系统通信模块不可用，不能接收和处理外部消息，造成节点故障
4. 使用场景：客户端的数量有限，业务处理非常快速，比如 `Redis` 在业务处理的时间复杂度 `O(1)` 的情况

## 5. 单 Reactor 多线程

### 方案说明

![image-20220210111436169](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220210111436169.png)

1. `Reactor` 对象通过 `Select` 监控客户端请求事件，收到事件后，通过 `Dispatch` 进行分发
2. 如果是建立连接请求，则由 `Acceptor` 通过 `accept` 处理连接请求，然后创建一个 `Handler` 对象处理完成连接后的各种事件
3. 如果不是连接请求，则由 `Reactor` 分发调用连接对应的 `handler` 来处理（也就是说连接已经建立，后续客户端再来请求，那基本就是数据请求了，直接调用之前为这个连接创建好的handler来处理）
4. `handler` 只负责响应事件，不做具体的业务处理（这样不会使handler阻塞太久），通过 `read` 读取数据后，会分发给后面的 `worker` 线程池的某个线程处理业务。【业务处理是最费时的，所以将业务处理交给线程池去执行】
5. `worker` 线程池会分配独立线程完成真正的业务，并将结果返回给 `handler`
6. `handler` 收到响应后，通过 `send` 将结果返回给 `client`

### 方案优缺点分析

1. 优点：可以充分的利用多核 `cpu` 的处理能力
2. 缺点：多线程数据共享和访问比较复杂。`Reactor` 承担所有的事件的监听和响应，它是单线程运行，在高并发场景容易出现性能瓶颈。也就是说`Reactor`主线程承担了过多的事

## 6. 主从 Reactor 多线程

### 工作原理图

针对单 `Reactor` 多线程模型中，`Reactor` 在单线程中运行，高并发场景下容易成为性能瓶颈，可以让 `Reactor` 在多线程中运行

![image-20220210113323858](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220210113323858.png)

1. `Reactor` 主线程 `MainReactor` 对象通过 `select` 监听连接事件，收到事件后，通过 `Acceptor` 处理连接事件
2. 当 `Acceptor` 处理连接事件后，`MainReactor` 将连接分配给 `SubReactor`
3. `subreactor` 将连接加入到连接队列进行监听，并创建 `handler` 进行各种事件处理
4. 当有新事件发生时，`subreactor` 就会调用对应的 `handler` 处理
5. `handler` 通过 `read` 读取数据，分发给后面的 `worker` 线程处理
6. `worker` 线程池分配独立的 `worker` 线程进行业务处理，并返回结果
7. `handler` 收到响应的结果后，再通过 `send` 将结果返回给 `client`
8. `Reactor` 主线程可以对应多个 `Reactor` 子线程，即 `MainRecator` 可以关联多个 `SubReactor`

### 方案优缺点说明

1. 优点：父线程与子线程的数据交互简单职责明确，父线程只需要接收新连接，子线程完成后续的业务处理。
2. 优点：父线程与子线程的数据交互简单，`Reactor` 主线程只需要把新连接传给子线程，子线程无需返回数据。
3. 缺点：编程复杂度较高

结合实例：这种模型在许多项目中广泛使用，包括 `Nginx` 主从 `Reactor` 多进程模型，`Memcached` 主从多线程，`Netty` 主从多线程模型的支持

## 7. Reactor 模式小结

### 3 种模式用生活案例来理解

1. 单 `Reactor` 单线程，前台接待员和服务员是同一个人，全程为顾客服务
2. 单 `Reactor` 多线程，`1` 个前台接待员，多个服务员，接待员只负责接待
3. 主从 `Reactor` 多线程，多个前台接待员，多个服务生

### Reactor 模式具有如下的优点

1. 响应快，不必为单个同步时间所阻塞，虽然 `Reactor` 本身依然是同步的（比如你第一个SubReactor阻塞了，我可以调下一个 SubReactor为客户端服务）
2. 可以最大程度的避免复杂的多线程及同步问题，并且避免了多线程/进程的切换开销
3. 扩展性好，可以方便的通过增加 `Reactor` 实例个数来充分利用 `CPU` 资源
4. 复用性好，`Reactor` 模型本身与具体事件处理逻辑无关，具有很高的复用性

## 8. Netty 模型

> 讲解netty的时候采用的是先写代码体验一下，再细讲里面的原理。前面看不懂的可以先不用纠结，先往后面看，后面基本都会讲清楚

### 工作原理示意图1 - 简单版

Netty主要基于主从 `Reactors` 多线程模型（如图）做了一定的改进，其中主从 `Reactor` 多线程模型有多个 `Reactor` 

![image-20220210165908913](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220210165908913.png)

**对上图说明**

1. `BossGroup` 线程维护 `Selector`，只关注 `Accept`
2. 当接收到 `Accept` 事件，获取到对应的 `SocketChannel`，封装成 `NIOScoketChannel` 并注册到 `Worker` 线程（事件循环），并进行维护
3. 当 `Worker` 线程监听到 `Selector` 中通道发生自己感兴趣的事件后，就进行处理（就由 `handler`），注意 `handler` 已经加入到通道

### 工作原理示意图2 - 进阶版

![image-20220210170818431](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220210170818431.png)

`BossGroup`有点像主`Reactor` 可以有多个，`WorkerGroup`则像`SubReactor`一样可以有多个。

### 工作原理示意图3 - 详细版

![image-20220328222331823](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220328222331823.png)

1. `Netty` 抽象出两组线程池 ，`BossGroup` 专门负责接收客户端的连接，`WorkerGroup` 专门负责网络的读写
2. `BossGroup` 和 `WorkerGroup` 类型都是 `NioEventLoopGroup`
3. `NioEventLoopGroup` 相当于一个事件循环组，这个组中含有多个事件循环，每一个事件循环是 `NioEventLoop`
4. `NioEventLoop` 表示一个不断循环的执行处理任务的线程，每个 `NioEventLoop` 都有一个 `Selector`，用于监听绑定在其上的 `socket` 的网络通讯
5. `NioEventLoopGroup` 可以有多个线程，即可以含有多个 `NioEventLoop`
6. 每个`BossGroup`下面的`NioEventLoop`循环执行的步骤有3步
   - 轮询 `accept` 事件
   - 处理 `accept` 事件，与 `client` 建立连接，生成 `NioScocketChannel`，并将其注册到某个 `workerGroup` `NIOEventLoop` 上的 `Selector`
   - 继续处理任务队列的任务，即 `runAllTasks`
7. 每个`WorkerGroup`、`NIOEventLoop`循环执行的步骤
   - 轮询 `read`，`write` 事件
   - 处理 `I/O` 事件，即 `read`，`write` 事件，在对应 `NioScocketChannel` 处理
   - 处理任务队列的任务，即 `runAllTasks`
8. 每个 `Worker` `NIOEventLoop` 处理业务时，会使用 `pipeline`（管道），`pipeline` 中包含了 `channel（通道）`，即通过 `pipeline` 可以获取到对应通道，管道中维护了很多的处理器。（这个点目前只是简单的讲，后面重点说）

### Netty 快速入门实例 - TCP 服务

实例要求：使用 `IDEA` 创建 `Netty` 项目

1. `Netty` 服务器在 `6668` 端口监听，客户端能发送消息给服务器"hello,服务器~"
2. 服务器可以回复消息给客户端"hello,客户端~"
3. 目的：对 `Netty` 线程模型有一个初步认识，便于理解 `Netty` 模型理论
4. 1. 编写服务端
   2. 编写客户端
   3. 对 `netty` 程序进行分析，看看 `netty` 模型特点
   4. 说明：创建 `Maven` 项目，并引入 `Netty` 包
5. 代码如下

NettyServer

```java
package com.fyp.netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

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
                            ch.pipeline().addLast(new NettyServerHandler());
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
package com.fyp.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;


/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: nettty服务端处理器
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取数据
     * @param ctx 上下文对象，含有 管道 pipeline, 通道 channel, 地址
     * @param msg 客户端发送的 数据， 默认类型 Object
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("服务器读取线程 " + Thread.currentThread().getName());

        // 不能 调用 父类的 channelRead方法，否则 会报 以下异常：
        // An exceptionCaught() event was fired, and it reached at the tail of the pipeline.
        // It usually means the last handler in the pipeline did not handle the exception
        //super.channelRead(ctx, msg);

        System.out.println("server ctx = " + ctx);

        // 将 msg 转成一个 ByteBuffer

        ByteBuf buf = (ByteBuf) msg;
        System.out.println("客户端发送消息是： " + buf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址：" + ctx.channel().remoteAddress());
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
package com.fyp.netty.simple;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


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
package com.fyp.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
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
        super.channelActive(ctx);

        System.out.println("client ctx = " + ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello,server: (>^ω^<)喵", CharsetUtil.UTF_8));
    }

    // 当通道 有读取事件 时 ，会触发
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        ByteBuf buf = (ByteBuf) msg;
        System.out.println("服务端回复的消息： " + buf.toString());
        System.out.println("服务端的地址： " + ctx.channel().remoteAddress());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        cause.printStackTrace();

        ctx.close();
    }
}
```

### 任务队列中的 Task 有 3 种典型使用场景

1. 用户程序自定义的普通任务【举例说明】
2. 用户自定义定时任务
3. 非当前 `Reactor` 线程调用 `Channel` 的各种方法 

​	例如在**推送系统**的业务线程里面，根据用户的标识，找到对应的 `Channel` 引用，然后调用 `Write` 类方法向该用户推送消息，就会进入到这种场景。最终的 `Write` 会提交到任务队列中后被异步消费

前两种的代码举例：

```java
package com.atguigu.netty.simple;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * 说明
 * 1. 我们自定义一个Handler 需要继续netty 规定好的某个HandlerAdapter(规范)
 * 2. 这时我们自定义一个Handler , 才能称为一个handler
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    //读取数据实际(这里我们可以读取客户端发送的消息)

    /**
     * 1. ChannelHandlerContext ctx:上下文对象, 含有 管道pipeline , 通道channel, 地址
     * 2. Object msg: 就是客户端发送的数据 默认Object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // 比如这里我们有一个非常耗时长的业务-> 异步执行 -> 提交该channel 对应的
        // NIOEventLoop 的 taskQueue中,

        // 解决方案1 用户程序自定义的普通任务

        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵2", CharsetUtil.UTF_8));
                    System.out.println("channel code=" + ctx.channel().hashCode());
                } catch (Exception ex) {
                    System.out.println("发生异常" + ex.getMessage());
                }
            }
        });

        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵3", CharsetUtil.UTF_8));
                    System.out.println("channel code=" + ctx.channel().hashCode());
                } catch (Exception ex) {
                    System.out.println("发生异常" + ex.getMessage());
                }
            }
        });

        //解决方案2 : 用户自定义定时任务 -》 该任务是提交到 scheduleTaskQueue中

        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵4", CharsetUtil.UTF_8));
                    System.out.println("channel code=" + ctx.channel().hashCode());
                } catch (Exception ex) {
                    System.out.println("发生异常" + ex.getMessage());
                }
            }
        }, 5, TimeUnit.SECONDS);

        System.out.println("go on ...");

//        System.out.println("服务器读取线程 " + Thread.currentThread().getName() + " channle =" + ctx.channel());
//        System.out.println("server ctx =" + ctx);
//        System.out.println("看看channel 和 pipeline的关系");
//        Channel channel = ctx.channel();
//        ChannelPipeline pipeline = ctx.pipeline(); //本质是一个双向链接, 出站入站
//        
//        //将 msg 转成一个 ByteBuf
//        //ByteBuf 是 Netty 提供的，不是 NIO 的 ByteBuffer.
//        ByteBuf buf = (ByteBuf) msg;
//        System.out.println("客户端发送消息是:" + buf.toString(CharsetUtil.UTF_8));
//        System.out.println("客户端地址:" + channel.remoteAddress());
    }

    //数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //writeAndFlush 是 write + flush
        //将数据写入到缓存，并刷新
        //一般讲，我们对这个发送的数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵1", CharsetUtil.UTF_8));
    }

    //处理异常, 一般是需要关闭通道
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
```

### 方案再说明

1. `Netty` 抽象出两组线程池，`BossGroup` 专门负责接收客户端连接，`WorkerGroup` 专门负责网络读写操作。
2. `NioEventLoop` 表示一个不断循环执行处理任务的线程，每个 `NioEventLoop` 都有一个 `Selector`，用于监听绑定在其上的 `socket`网络通道。
3. `NioEventLoop` 内部采用串行化设计，从消息的 **读取->解码->处理->编码->发送**，始终由 `IO` 线程 `NioEventLoop` 负责

- `NioEventLoopGroup` 下包含多个 `NioEventLoop`
- 每个 `NioEventLoop` 中包含有一个 `Selector`，一个 `taskQueue`
- 每个 `NioEventLoop` 的 `Selector` 上可以注册监听多个 `NioChannel`
- 每个 `NioChannel` 只会绑定在唯一的 `NioEventLoop` 上
- 每个 `NioChannel` 都绑定有一个自己的 `ChannelPipeline`

## 9. 异步模型

### 基本介绍

1. 异步的概念和同步相对。当一个异步过程调用发出后，调用者不能立刻得到结果。实际处理这个调用的组件在完成后，通过状态、通知和回调来通知调用者。
2. `Netty` 中的 `I/O` 操作是异步的，包括 `Bind、Write、Connect` 等操作会首先简单的返回一个 `ChannelFuture`。
3. 调用者并不能立刻获得结果，而是通过 `Future-Listener` 机制，用户可以方便的主动获取或者通过通知机制获得 `IO` 操作结果。
4. `Netty` 的异步模型是建立在 `future` 和 `callback` 的之上的。`callback` 就是回调。重点说 `Future`，它的核心思想是：假设一个方法 `fun`，计算过程可能非常耗时，等待 `fun` 返回显然不合适。那么可以在调用 `fun` 的时候，立马返回一个 `Future`，后续可以通过 `Future` 去监控方法 `fun` 的处理过程（即：`Future-Listener` 机制）

### Future 说明

1. 表示异步的执行结果,可以通过它提供的方法来检测执行是否完成，比如检索计算等等。
2. `ChannelFuture` 是一个接口：`public interface ChannelFuture extends Future<Void>` 我们可以添加监听器，当监听的事件发生时，就会通知到监听器。

### 工作原理示意图

下面第一张图就是管道，中间会经过多个handler

![image-20220212154019747](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220212154019747.png)

说明：

1. 在使用 `Netty` 进行编程时，拦截操作和转换出入站数据只需要您提供 `callback` 或利用 `future` 即可。这使得链式操作简单、高效，并有利于编写可重用的、通用的代码。
2. `Netty` 框架的目标就是让你的业务逻辑从网络基础应用编码中分离出来、解脱出来。

### Future-Listener 机制

> 这里看不懂的可以看笔者的**并发系列-JUC部分**

1. 当 `Future` 对象刚刚创建时，处于非完成状态，调用者可以通过返回的 `ChannelFuture` 来获取操作执行的状态，注册监听函数来执行完成后的操作。
2. 常见有如下操作
   - 通过 `isDone` 方法来判断当前操作是否完成；
   - 通过 `isSuccess` 方法来判断已完成的当前操作是否成功；
   - 通过 `getCause` 方法来获取已完成的当前操作失败的原因；
   - 通过 `isCancelled` 方法来判断已完成的当前操作是否被取消；
   - 通过 `addListener` 方法来注册监听器，当操作已完成（`isDone`方法返回完成），将会通知指定的监听器；如果 `Future` 对象已完成，则通知指定的监听器

举例说明 演示：绑定端口是异步操作，当绑定操作处理完，将会调用相应的监听器处理逻辑

```java
//绑定一个端口并且同步,生成了一个ChannelFuture对象
//启动服务器(并绑定端口)
ChannelFuture cf = bootstrap.bind(6668).sync();
//给cf注册监听器，监控我们关心的事件
cf.addListener(new ChannelFutureListener() {
   @Override
   public void operationComplete (ChannelFuture future) throws Exception {
      if (future.isSuccess()) {
         System.out.println("监听端口6668成功");
      } else {
         System.out.println("监听端口6668失败");
      }
   }
});
```

### 快速入门实例 - HTTP服务

1. 实例要求：使用 `IDEA` 创建 `Netty` 项目
2. `Netty` 服务器在 `9999` 端口监听，浏览器发出请求 `http://localhost:9999/`
3. 服务器可以回复消息给客户端"Hello!我是服务器5",并对特定请求资源进行过滤。
4. 目的：`Netty` 可以做 `Http` 服务开发，并且理解 `Handler` 实例和客户端及其请求的关系。
5. 代码演示

TestServer

```java
package com.fyp.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/12
 * @Description: 测试http服务端
 * @Package: com.fyp.netty.http
 * @Version: 1.0
 */
public class TestServer {
    public static void main(String[] args) throws Exception{

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new TestServerInitializer());

            ChannelFuture channelFuture = serverBootstrap.bind(9999).sync();

            channelFuture.channel().closeFuture().sync();


        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
```

TestHttpServeHandler

```java
package com.fyp.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;

/**
 * @Auther: fyp
 * @Date: 2022/2/12
 * @Description: 测试http服务端处理器
 * @Package: com.fyp.netty.http
 * @Version: 1.0
 */
public class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        // 判断 msg 是不是 httpRequest请求
        if(msg instanceof HttpRequest) {

            System.out.println("pipeline hashcode: " + ctx.pipeline().hashCode() + " TestHttpServerHandler hashcode: " + this.hashCode());

            System.out.println("msg 类型：" + msg.getClass());
            System.out.println("客户端地址：" + ctx.channel().remoteAddress());

            // 获取 HttpRequest
            HttpRequest httpRequest = (HttpRequest) msg;
            // 获取 uri
            URI uri = new URI(httpRequest.uri());

            System.out.println("请求路径： " + uri);

            if ("/favicon.ico".equals(uri.getPath())) {
                System.out.println("请求了favicon.ico, 不做响应");
                return;
            }


            // 回复信息 给 浏览器 [http 协议]
            ByteBuf content = Unpooled.copiedBuffer("hello 我是服务器", CharsetUtil.UTF_8);

            // 构造一个 http 响应
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

            //将构建好 response返回，必要一步，没有客户端 将收不到服务端 发送的数据
            ctx.writeAndFlush(response);

        }

    }
}
```

TestServerInitializer

```java
package com.fyp.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @Auther: fyp
 * @Date: 2022/2/12
 * @Description: 测试http服务端的初始化器
 * @Package: com.fyp.netty.http
 * @Version: 1.0
 */
public class TestServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        // 向 管道 加入 处理器

        // 得到 管道
        ChannelPipeline pipeline = ch.pipeline();

        // 加入 一个 netty 提供的  httpServerCodec => [coder - decoder]
        /*
            HttpServerCodec 说明
            1. HttpServerCodec 是 netty 提供的 处理 http 的编解码器
            2. 增加一个自定义的 handler
         */
        pipeline.addLast("MyHttpServerCodec", new HttpServerCodec());
        pipeline.addLast("MyTestHttpServerHandler", new TestHttpServerHandler());

        System.out.println("ok ~");

    }
}
```

# 三、Netty 核心模块组件

### Bootstrap、ServerBootstrap

1. `Bootstrap` 意思是引导，一个 `Netty` 应用通常由一个 `Bootstrap` 开始，主要作用是配置整个 `Netty` 程序，串联各个组件，`Netty` 中 `Bootstrap` 类是客户端程序的启动引导类，`ServerBootstrap` 是服务端启动引导类。
2. 常见的方法有
   - `public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup)`，该方法用于服务器端，用来设置两个 `EventLoop`
   - `public B group(EventLoopGroup group)`，该方法用于客户端，用来设置一个 `EventLoop`
   - `public B channel(Class<? extends C> channelClass)`，该方法用来设置一个服务器端的通道实现
   - `public <T> B option(ChannelOption<T> option, T value)`，用来给 `ServerChannel` 添加配置
   - `public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value)`，用来给接收到的通道添加配置
   - `public ServerBootstrap childHandler(ChannelHandler childHandler)`，该方法用来设置业务处理类（自定义的`handler`）
   - `public ChannelFuture bind(int inetPort)`，该方法用于服务器端，用来设置占用的端口号
   - `public ChannelFuture connect(String inetHost, int inetPort)`，该方法用于客户端，用来连接服务器端

### Future、ChannelFuture

`Netty` 中所有的 `IO` 操作都是异步的，不能立刻得知消息是否被正确处理。但是可以过一会等它执行完成或者直接注册一个监听，具体的实现就是通过 `Future` 和 `ChannelFutures`，他们可以注册一个监听，当操作执行成功或失败时监听会自动触发注册的监听事件

常见的方法有

- `Channel channel()`，返回当前正在进行 `IO` 操作的通道
- `ChannelFuture sync()`，等待异步操作执行完毕

### Channel

1. `Netty` 网络通信的组件，能够用于执行网络 `I/O` 操作。
2. 通过 `Channel` 可获得当前网络连接的通道的状态
3. 通过 `Channel` 可获得网络连接的配置参数（例如接收缓冲区大小）
4. `Channel` 提供异步的网络 `I/O` 操作(如建立连接，读写，绑定端口)，异步调用意味着任何 `I/O` 调用都将立即返回，并且不保证在调用结束时所请求的 `I/O` 操作已完成
5. 调用立即返回一个 `ChannelFuture` 实例，通过注册监听器到 `ChannelFuture` 上，可以 `I/O` 操作成功、失败或取消时回调通知调用方
6. 支持关联 `I/O` 操作与对应的处理程序
7. 不同协议、不同的阻塞类型的连接都有不同的`Channel`类型与之对应，常用的`Channel`类型：
   - `NioSocketChannel`，异步的客户端 `TCP` `Socket` 连接。
   - `NioServerSocketChannel`，异步的服务器端 `TCP` `Socket` 连接。
   - `NioDatagramChannel`，异步的 `UDP` 连接。
   - `NioSctpChannel`，异步的客户端 `Sctp` 连接。
   - `NioSctpServerChannel`，异步的 `Sctp` 服务器端连接，这些通道涵盖了 `UDP` 和 `TCP` 网络 `IO` 以及文件 `IO`。

### Selector

1. `Netty` 基于 `Selector` 对象实现 `I/O` 多路复用，通过 `Selector` 一个线程可以监听多个连接的 `Channel` 事件。
2. 当向一个 `Selector` 中注册 `Channel` 后，`Selector` 内部的机制就可以自动不断地查询（`Select`）这些注册的 `Channel` 是否有已就绪的 `I/O` 事件（例如可读，可写，网络连接完成等），这样程序就可以很简单地使用一个线程高效地管理多个 `Channel`

### ChannelHandler 及其实现类

1. `ChannelHandler` 是一个接口，处理 `I/O` 事件或拦截 `I/O` 操作，并将其转发到其 `ChannelPipeline`（业务处理链）中的下一个处理程序。

2. `ChannelHandler` 本身并没有提供很多方法，因为这个接口有许多的方法需要实现，方便使用期间，可以继承它的子类

3. `ChannelHandler` 及其实现类一览图（后）

   ![image-20220213130921952](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220213130921952.png)

4. 我们经常需要自定义一个 `Handler` 类去继承 `ChannelInboundHandlerAdapter`，然后通过重写相应方法实现业务逻辑，我们接下来看看一般都需要重写哪些方法

![image-20220213141448691](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220213141448691.png)

### Pipeline 和 ChannelPipeline

`ChannelPipeline` 是一个重点：

1. `ChannelPipeline` 是一个 `Handler` 的集合，它负责处理和拦截 `inbound` 或者 `outbound` 的事件和操作，相当于一个贯穿 `Netty` 的链。（也可以这样理解：`ChannelPipeline` 是保存 `ChannelHandler` 的 `List`，用于处理或拦截 `Channel` 的入站事件和出站操作）

2. `ChannelPipeline` 实现了一种高级形式的拦截过滤器模式，使用户可以完全控制事件的处理方式，以及 `Channel` 中各个的 `ChannelHandler` 如何相互交互

3. 在 `Netty` 中每个 `Channel` 都有且仅有一个 `ChannelPipeline` 与之对应，它们的组成关系如下

   ![image-20220213133509950](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220213133509950.png)

   1. 一个Channel包含了一个ChannelPipeline，而ChannelPipeline中又维护了一个由ChannelHandlerContext组成的双向链表，并且每个ChannelHandlerContext中又关联着一个ChannelHandler
   2. 入站事件和出战事件在一个双向链表中，入站事件会从链表head往后传递到最后一个入站的handler，出站事件会重链表tail往前传递到最前一个出站的handler，两种类型的handler互不干扰

4. 常用方法

   1. `ChannelPipeline addFirst(ChannelHandler... handlers)`，把一个业务处理类（`handler`）添加到链中的第一个位置
   2. `ChannelPipeline addLast(ChannelHandler... handlers)`，把一个业务处理类（`handler`）添加到链中的最后一个位置
   3. `TestServerInitializer`和`HttpServerCodec`这些东西本身也是`handler`

   1. 一般来说事件从客户端往服务器走我们称为出站，反之则是入站。

### ChannelHandlerContext

1. 保存 `Channel` 相关的所有上下文信息，同时关联一个 `ChannelHandler` 对象
2. 即 `ChannelHandlerContext` 中包含一个具体的事件处理器 `ChannelHandler`，同时 `ChannelHandlerContext` 中也绑定了对应的 `pipeline` 和 `Channel` 的信息，方便对 `ChannelHandler` 进行调用。
3. 常用方法
   1. `ChannelFuture close()`，关闭通道
   2. `ChannelOutboundInvoker flush()`，刷新
   3. `ChannelFuture writeAndFlush(Object msg)`，将数据写到
   4. `ChannelPipeline` 中当前 `ChannelHandler` 的下一个 `ChannelHandler` 开始处理（出站）

### ChannelOption

1. `Netty` 在创建 `Channel` 实例后，一般都需要设置 `ChannelOption` 参数。

2. `ChannelOption` 参数如下：

   ![image-20220213142930737](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220213142930737.png)

### EventLoopGroup 和其实现类 NioEventLoopGroup

1. `EventLoopGroup` 是一组 `EventLoop` 的抽象，`Netty` 为了更好的利用多核 `CPU` 资源，一般会有多个 `EventLoop` 同时工作，每个 `EventLoop` 维护着一个 `Selector` 实例。

2. `EventLoopGroup` 提供 `next` 接口，可以从组里面按照一定规则获取其中一个 `EventLoop` 来处理任务。在 `Netty` 服务器端编程中，我们一般都需要提供两个 `EventLoopGroup`，例如：`BossEventLoopGroup` 和 `WorkerEventLoopGroup`。

3. 通常一个服务端口即一个 `ServerSocketChannel` 对应一个 `Selector` 和一个 `EventLoop` 线程。`BossEventLoop` 负责接收客户端的连接并将 `SocketChannel` 交给 `WorkerEventLoopGroup` 来进行 `IO` 处理，如下图所示

   ![image-20220213143425262](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220213143425262.png)

4. 常用方法 `public NioEventLoopGroup()`，构造方法 `public Future<?> shutdownGracefully()`，断开连接，关闭线程

### Unpooled 类

1. `Netty` 提供一个专门用来操作缓冲区（即 `Netty` 的数据容器）的工具类

2. 常用方法如下所示

   ![image-20220213210356259](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220213210356259.png)

3. 举例说明 `Unpooled` 获取 `Netty` 的数据容器 `ByteBuf` 的基本使用

   ![image-20220213210527196](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220213210527196.png)~

案例1：

```java
package com.fyp.netty.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @Auther: fyp
 * @Date: 2022/2/13
 * @Description: ByteBuf测试
 * @Package: com.fyp.netty.buf
 * @Version: 1.0
 */
public class NettyByteBuf01 {
    public static void main(String[] args) {
        /*
        创建一个ByteBuf
            说明:
            1. 创建对象，该对象 包含一个 数组 arr，是一个 byte[10]
            2. 在 netty 的 buffer 中，不需要 使用 flip 进行 反转
                底层 维护了 readIndex 和 writeIndex
            3. 通过 readerIndex 和 writerIndex 和 capacity 将 buffer 分为 三个 区域
            0 - readerIndex 已经 读取的 区域
            readrIndex - writeIndex 可读的 区域
            writerIndex - capacity 可写的 区域
         */

        ByteBuf buffer = Unpooled.buffer(10);

        for (int i =0; i < 10; i++) {
            buffer.writeByte(i);
        }

        System.out.println("capacity: " + buffer.capacity());

        //for (int i = 0; i < buffer.capacity(); i++) {
        //    System.out.println(buffer.getByte(i));
        //}

        for (int i = 0; i < buffer.capacity(); i++) {
            System.out.println(buffer.readByte());
        }

    }
}
```

案例2

````java
package com.fyp.netty.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * @Auther: fyp
 * @Date: 2022/2/13
 * @Description: ByteBuf测试
 * @Package: com.fyp.netty.buf
 * @Version: 1.0
 */
public class NettyByteBuf02 {
    public static void main(String[] args) {

        // 创建 ByteBuf
        ByteBuf buf = Unpooled.copiedBuffer("hello,world!", CharsetUtil.UTF_8);

        if(buf.hasArray()) { //true

            byte[] content = buf.array();

            // 将 content 转成 字符串
            System.out.println(new String(content, CharsetUtil.UTF_8));

            System.out.println("ByteBuf= " + buf);

            System.out.println(buf.arrayOffset());
            System.out.println(buf.readerIndex());
            System.out.println(buf.writerIndex());
            System.out.println(buf.capacity());

            int len = buf.readableBytes(); // 可读的 字节数
            System.out.println("len= " + len);

            // 使用 for 取出 各个字符
            for (int i = 0; i < len; i++) {
                System.out.println((char) buf.getUnsignedByte(i));
            }

            System.out.println(buf.getCharSequence(0, 4, CharsetUtil.UTF_8));
            System.out.println(buf.getCharSequence(4, 6, CharsetUtil.UTF_8));
        }
        
    }
}
````

### Netty 应用实例-群聊系统

实例要求：

1. 编写一个 `Netty` 群聊系统，实现服务器端和客户端之间的数据简单通讯（非阻塞）
2. 实现多人群聊
3. 服务器端：可以监测用户上线，离线，并实现消息转发功能
4. 客户端：通过 `channel` 可以无阻塞发送消息给其它所有用户，同时可以接受其它用户发送的消息（有服务器转发得到）
5. 目的：进一步理解 `Netty` 非阻塞网络编程机制

![image-20220214212546804](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220214212546804.png)



![image-20220214212517306](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220214212517306.png)

`NioSocketChannel`和`NioSocketChannel`实现的接口

![image-20220321202152612](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220321202152612.png)

`NioSocketChannel`和`NioSocketChannel`继承的类

![image-20220321202334252](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220321202334252.png)

代码如下：

GroupChatServer

```java
package com.fyp.netty.groupchat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description: 群聊系统服务端
 * @Package: com.fyp.netty.groupchat
 * @Version: 1.0
 */
public class GroupChatServer {
    private int port; // 监听端口

    public GroupChatServer(int port) {
        this.port = port;
    }

    // 编写 run 方法，处理 客户端的 请求
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            // 获取到 pipeline
                            ChannelPipeline pipeline = ch.pipeline();
                            // 向 pipeline 加入 解码器
                            pipeline.addLast("decoder", new StringDecoder());
                            // 向 pipeline 加入 编码器
                            pipeline.addLast("encoder", new StringEncoder());
                            // 加入自己 业务处理的 handler
                            pipeline.addLast(new GroupChatServerHandler());

                        }
                    });

            System.out.println("netty 服务器启动...");
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            // 监听 关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new GroupChatServer(7000).run();
    }

}
```

GroupChatServerHandler

```java
package com.fyp.netty.groupchat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description: 群聊系统服务端处理器
 * @Package: com.fyp.netty.groupchat
 * @Version: 1.0
 */
public class GroupChatServerHandler extends SimpleChannelInboundHandler<String> {

    // 定义 一个 channel 组， 管理所有的 channel
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     *  handlerAdded 表示连接建立， 一旦连接， 第一个被执行
     *  将当前的 channel 加入到 channelGroup
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        Channel channel = ctx.channel();
        // 将该客户端加入 聊天的信息 ，推送给 其他 客户端
        /*
        该方法 不会 将 channelGroup 中 所有的 channel 遍历，并发送 消息
        我们 不需要 自己 遍历
         */
        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + " 加入聊天 " + sdf.format(new Date()) + "\t");
        channelGroup.add(channel);
    }

    // 断开 连接，将 xx 客户端 离开信息 推送给 其他 在线的 客户端
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        Channel channel = ctx.channel();

        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + " 离开了 \t");

        System.out.println("channelGroup size " + channelGroup.size());

    }

    // 表示 channel 处于 活动状态，提示 xx 在线
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println(ctx.channel().remoteAddress() + "上线了~");

    }

    // 表示 channel 处于 不活动状态，提示 xx 下线
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        System.out.println(ctx.channel().remoteAddress() + "离线了~");

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        // 获取到当前的 channel
        Channel channel = ctx.channel();
        // 遍历 channelGroup，根据不同的 情况，回送 不同的 消息

        channelGroup.forEach(ch -> {
            if (channel != ch) { // 不是当前的 channel，转发消息
                ch.writeAndFlush("[客户] " + channel.remoteAddress() + " 发送了消息： " + msg + "\n");
            } else {// 回显 自己发送的消息 给自己
                ch.writeAndFlush("[自己] 发送了消息： " + msg + "\n");
            }
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 关闭通道
        ctx.close();
    }
}
```

GroupChatClient

```java
package com.fyp.netty.groupchat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description: 群聊客户端
 * @Package: com.fyp.netty.groupchat
 * @Version: 1.0
 */
public class GroupChatClient {

    //属性
    private final String host;
    private final int port;

    public GroupChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {


            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            // 得到 pipeline
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast(new GroupChatClientHandler());

                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();

            Channel channel = channelFuture.channel();
            System.out.println("-----" + channel.localAddress() + "-----");
            // 客户端 需要 输入信息， 创建一个 扫描器
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String msg = scanner.nextLine();
                // 通过 channel 发送到 服务器端
                channel.writeAndFlush(msg + "\r\n");
            }

        } finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws Exception{
        new GroupChatClient("127.0.0.1", 7000).run();
    }

}
```

GroupChatClientHandler

```java
package com.fyp.netty.groupchat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description: 群聊系统客户端处理器
 * @Package: com.fyp.netty.groupchat
 * @Version: 1.0
 */
public class GroupChatClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg.trim());
    }
}
```

### Netty 心跳检测机制案例

实例要求：

1. 编写一个 `Netty` 心跳检测机制案例,当服务器超过 `3` 秒没有读时，就提示读空闲
2. 当服务器超过 `5` 秒没有写操作时，就提示写空闲
3. 实现当服务器超过 `7` 秒没有读或者写操作时，就提示读写空闲
4. 代码如下：

MyServer

```java
package com.fyp.netty.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;
import java.util.jar.Pack200;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description:
 * @Package: com.fyp.netty.heartbeat
 * @Version: 1.0
 */
public class MyServer {

    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

                ServerBootstrap serverBootstrap = new ServerBootstrap();

                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.INFO)) // 在 bossGroup 中 增加一个 日志 处理器
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                // 加入一个 netty 提供的 IdleStateHandler
                                /*
                                说明
                                 1. IdleStateHandler 是 netty 提供的 处理 空闲状态的 处理器
                                 2.readerIdleTime 表示多长时间 没有读，就会发送一个 心跳检测包 检测是否连接
                                 3. writerIdleTime 表示多长时间 没有写，就会发送一个 心跳检测包 检测是否连接
                                 4. allIdleTime 表示多长时间 没有读写，就会发送一个 心跳检测包 检测是否连接
                                 5. 文档说明
                                 Triggers an {@link IdleStateEvent} when a {@link Channel} has not performed
                                 read, write, or both operation for a while.
                                 6. 当 IdleStateEvent 触发后， 就会 传递给管道 的下一个 handler 去处理
                                 通过 调用（触发）下一个 handler 的 userEventTriggered, 在该方法中去处理
                                 IdleStateEvent（读空闲，写空闲，读写空闲）
                                 */
                                pipeline.addLast(new IdleStateHandler(3, 5, 7 , TimeUnit.SECONDS));
                                // 加入一个 对 空闲检测 进行处理的 handler
                                pipeline.addLast(new MyServerHandler());
                            }
                        });

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
package com.fyp.netty.heartbeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description: MyServer 处理器
 * @Package: com.fyp.netty.heartbeat
 * @Version: 1.0
 */
public class MyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     *
     * @param ctx 上下文
     * @param evt 事件
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {

            // 将 evt 向下转型为 IdleStateEvent
            IdleStateEvent event = (IdleStateEvent) evt;
            String eventType = null;

            switch (event.state()) {
                case READER_IDLE:
                    eventType = "读空闲";
                    break;
                case WRITER_IDLE:
                    eventType = "写空闲";
                    break;
                case ALL_IDLE:
                    eventType = "读写空闲";
                    break;
            }
            System.out.println(ctx.channel().remoteAddress() + "--超时事件--" + eventType);
            System.out.println("服务器做相应处理");

        }

    }
}
```

### Netty 通过 WebSocket 编程实现服务器和客户端长连接

实例要求：

1. `Http` 协议是无状态的，浏览器和服务器间的请求响应一次，下一次会重新创建连接。
2. 要求：实现基于 `WebSocket` 的长连接的全双工的交互
3. 改变 `Http` 协议多次请求的约束，实现长连接了，服务器可以发送消息给浏览器
4. 客户端浏览器和服务器端会相互感知，比如服务器关闭了，浏览器会感知，同样浏览器关闭了，服务器会感知
5. 运行界面

![image-20220214233202523](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220214233202523.png)

![image-20220214233235166](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220214233235166.png)

MyServer

```java
package com.fyp.netty.websocket;

import com.fyp.netty.heartbeat.MyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description:
 * @Package: com.fyp.netty.websocket
 * @Version: 1.0
 */
public class MyServer {
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO)) // 在 bossGroup 中 增加一个 日志 处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            // 因为 基于 http协议，使用 http的 编码和解码
                            pipeline.addLast(new HttpServerCodec());
                            // 是 以块方式 写，添加 ChunkedWriteHandler 处理器
                            pipeline.addLast(new ChunkedWriteHandler());

                            /*
                            说明
                                1. http数据 在传输过程中是分段的， HttpObjectAggregator 就是可以将
                                多个段 聚合
                                2.这就是为什么 当 浏览器 发送 大量数据时， 就会 发送 多次 http请求
                             */
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            /*
                            说明
                                1. 对应的 webSocket, 它的数据 以帧（frame）形式传递
                                2. 可以看到WebSocketFrame 下面有六个子类
                                3. 浏览器请求时， ws://localhost:7000/xxx 表示请求的uri
                                4. WebSocketServerProtocolHandler 核心功能是 将 http协议 升级为 ws 协议，保持长链接
                             */
                            pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));

                            // 自定义的 handler,处理业务逻辑
                            pipeline.addLast(new MyTextWebSocketFrameHandler());

                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(7000).sync();
            channelFuture.channel().closeFuture().sync();


        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }
}
```

MyTextWebSocketFrameHandler

```java
package com.fyp.netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.time.LocalDateTime;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description:
 * @Package: com.fyp.netty.websocket
 * @Version: 1.0
 */
public class MyTextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        System.out.println("服务端送到消息 " + msg.text());

        // 回复消息
        ctx.channel().writeAndFlush(new TextWebSocketFrame("服务器时间" + LocalDateTime.now() + " " + msg.text()));

    }

    // 当 客户端连接后， 触发方法
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // id 表示 唯一的值 LongText是唯一的 ShortText不是唯一的
        System.out.println("handlerAdded 被调用" + ctx.channel().id().asLongText());
        System.out.println("handlerAdded 被调用" + ctx.channel().id().asShortText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerRemoved 被调用" + ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常发生 " + cause.getMessage());
    }
}
```

hello.html

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>

<script>
    var socket;
    // 判断当前 浏览器 是否支持 webSocket
    if (window.WebSocket) {
        // go on
        // 相当于 channelRead0, ev收到服务器端回送的消息
        socket = new WebSocket("ws://localhost:7000/hello");
        socket.onmessage = function (ev) {
            var rt = document.getElementById("responseText");
            rt.value = rt.value + "\n" + ev.data;
        }
        // 相当于 连接开启（感知到连接开启）
        socket.onopen = function(ev) {
            var rt = document.getElementById("responseText");
            rt.value = "连接开启了...";
        }
        // 相当于 连接关闭（感知到连接关闭）
        socket.onclose = function (ev) {
            var rt = document.getElementById("responseText");
            rt.value = rt.value + "\n" + "连接关闭了...";
        }
    } else {
        alert("当前浏览器不支持webSocket")
    }

    // 发送消息到服务器
    function send(message) {
        if(!window.socket) { // 先判断 socket 是否创建好
            return;
        }
        if (socket.readyState == WebSocket.OPEN) {
            // 通过 socket 发送消息
            socket.send(message);
        } else {
            alert("连接没有开启");
        }
    }


</script>

    <form onsubmit="return false">
        <textarea name="message" style="height: 300px; width: 300px"></textarea>
        <input type="button" value="发送消息" onclick="send(this.form.message.value)"><br>
        <textarea id="responseText" style="height: 300px; width: 300px"></textarea>
        <input type="button" value="请空内容" onclick="document.getElementById('responseText').value=''">
    </form>

</body>
</html>
```
