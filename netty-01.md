Nettty入门01

# 一、 Netty简介

## 1. Netty介绍

1. `Netty` 是由 `JBOSS` 提供的一个 `Java` 开源框架，现为 `Github` 上的独立项目。
2. `Netty` 是一个异步的、基于事件驱动的网络应用框架，用以快速开发高性能、高可靠性的网络 `IO` 程序。
3. `Netty` 主要针对在 `TCP` 协议下，面向 `Client` 端的高并发应用，或者 `Peer-to-Peer` 场景下的大量数据持续传输的应用。
4. `Netty` 本质是一个 `NIO` 框架，适用于服务器通讯相关的多种应用场景。
5. 要透彻理解 `Netty`，需要先学习 `NIO`，这样我们才能阅读 `Netty` 的源码。

![image-20220131113601261](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220131113601261.png)

## 2. Netty的应用场景

### 互联网行业

1. 互联网行业：在分布式系统中，各个节点之间需要远程服务调用，高性能的 `RPC` 框架必不可少，`Netty` 作为异步高性能的通信框架，往往作为基础通信组件被这些 `RPC` 框架使用。
2. 典型的应用有：阿里分布式服务框架 `Dubbo` 的 `RPC` 框 架使用 `Dubbo` 协议进行节点间通信，`Dubbo` 协议默认使用 `Netty` 作为基础通信组件，用于实现各进程节点之间的内部通信。

### 游戏行业

1. 游戏行业 协议栈，方便定制和开发私有协议栈，账号登录服务器。
2. `Netty` 作为高性能的基础通信组件，提供了 `TCP/UDP` 和 `HTTP` 协议栈，方便定制和开发私有协议栈，账号登录服务器。
3. 地图服务器之间可以方便的通过 `Netty` 进行高性能的通信。

### 大数据领域

1. 经典的 `Hadoop` 的高性能通信和序列化组件 `Avro` 的 `RPC` 框架，默认采用 `Netty` 进行跨界点通信。
2. 它的 `NettyService` 基于 `Netty` 框架二次封装实现。

### 大数据行业

1. 经典的 `Hadoop` 的高性能通信和序列化组件 `Avro` 的 `RPC` 框架，默认采用 `Netty` 进行跨界点通信。
2. 它的 `NettyService` 基于 `Netty` 框架二次封装实现。

# 二、Java BIO编程

## 1. I/O模型

1. `I/O` 模型简单的理解：就是用什么样的通道进行数据的发送和接收，很大程度上决定了程序通信的性能。

2. `Java` 共支持 `3` 种网络编程模型 `I/O` 模式：`BIO`、`NIO`、`AIO`。

3. `Java BIO`：同步并阻塞（传统阻塞型），服务器实现模式为一个连接一个线程，即客户端有连接请求时服务器端就需要启动一个线程进行处理，如果这个连接不做任何事情会造成不必要的线程开销。【简单示意图】

   ![](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220131113716951.png)

4. `Java NIO`：同步非阻塞，服务器实现模式为一个线程处理多个请求（连接)，即客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有 `I/O` 请求就进行处理。【简单示意图】

   ![](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220131113924628.png)

5. `Java AIO(NIO.2)`：异步非阻塞，`AIO` 引入异步通道的概念，采用了 `Proactor` 模式，简化了程序编写，有效的请求才启动线程，它的特点是先由操作系统完成后才通知服务端程序启动线程去处理，一般适用于连接数较多且连接时间较长的应用。

## 2. BIO、NIO、AIO使用场景分析

1. `BIO` 方式适用于连接数目比较小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用中，`JDK1.4` 以前的唯一选择，但程序简单易理解。
2. `NIO` 方式适用于连接数目多且连接比较短（轻操作）的架构，比如聊天服务器，弹幕系统，服务器间通讯等。编程比较复杂，`JDK1.4` 开始支持。
3. `AIO` 方式使用于连接数目多且连接比较长（重操作）的架构，比如相册服务器，充分调用 `OS` 参与并发操作，编程比较复杂，`JDK7` 开始支持。

## 3. Java BIO基本介绍

1. `Java BIO` 就是传统的 `Java I/O` 编程，其相关的类和接口在 `java.io`。
2. `BIO(BlockingI/O)`：同步阻塞，服务器实现模式为一个连接一个线程，即客户端有连接请求时服务器端就需要启动一个线程进行处理，如果这个连接不做任何事情会造成不必要的线程开销，可以通过线程池机制改善（实现多个客户连接服务器）。【后有应用实例】
3. `BIO` 方式适用于连接数目比较小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用中，`JDK1.4` 以前的唯一选择，程序简单易理解。

## 4. Java BIO工作机制

![image-20220131114126473](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220131114126473.png)

对 `BIO` 编程流程的梳理

1. 服务器端启动一个 `ServerSocket`。
2. 客户端启动 `Socket` 对服务器进行通信，默认情况下服务器端需要对每个客户建立一个线程与之通讯。
3. 客户端发出请求后，先咨询服务器是否有线程响应，如果没有则会等待，或者被拒绝。
4. 如果有响应，客户端线程会等待请求结束后，再继续执行。

## 5. Java BIO应用实例

实例说明：

1. 使用 `BIO` 模型编写一个服务器端，监听 `6666` 端口，当有客户端连接时，就启动一个线程与之通讯。
2. 要求使用线程池机制改善，可以连接多个客户端。
3. 服务器端可以接收客户端发送的数据（`telnet` 方式即可）。
4. 代码演示：

```java
package com.fyp.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Auther: fyp
 * @Date: 2022/1/31
 * @Description: BIO服务
 * @Package: com.fyp.bio
 * @Version: 1.0
 */
public class BIOServer {
    public static void main(String[] args) throws IOException {
        /**
         * 思路：
         * 1. 创建一个线程池
         * 2. 如果有客户链接，就创建一个线程，与之通讯（单独写一个方法）
         */
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();

        //创建ServerSocket
        ServerSocket serverSocket = new ServerSocket(6666);

        System.out.println("服务器启动了");

        while (true) {
            System.out.println("等待连接");
            //监听，等待客户端链接
            final Socket socket = serverSocket.accept();
            System.out.println("连接到一个客户端");

            //创建一个线程，与之通讯（单独写一个方法）
            newCachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    //可以客户端通讯
                    handler(socket);
                }
            });


        }
    }

    //编写一个handle方法，和客户端通讯
    public static void handler(Socket socket) {
        try {
            System.out.println("线程信息 id = " + Thread.currentThread().getId() + " 名字 = " + Thread.currentThread().getName());
            byte[] bytes = new byte[10];
            //通过Socket获取输入流
            InputStream inputStream = socket.getInputStream();

            while (true) {
                System.out.println("read....");
                //没有读到数据会阻塞
                int read = inputStream.read(bytes);
                if(read != -1) {
                    System.out.println("线程信息 id = " + Thread.currentThread().getId() + " 名字 = " + Thread.currentThread().getName() + " -- " + new String(bytes, 0, read));
                } else {
                    System.out.println("break");
                    break;
                }
            }
            System.out.println("跳出read循环");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("关闭和client的连接");
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

```

## 6. 总结

1. 每个请求都需要创建独立的线程，与对应的客户端进行数据 `Read`，业务处理，数据 `Write`。
2. 当并发数较大时，需要创建大量线程来处理连接，系统资源占用较大。
3. 连接建立后，如果当前线程暂时没有数据可读，则线程就阻塞在 `Read` 操作上，造成线程资源浪费。

# 三、Java NIO编程

## 1. Java NIO基本介绍

1. `Java NIO` 全称 `Java non-blocking IO`，是指 `JDK` 提供的新 `API`。从 `JDK1.4` 开始，`Java` 提供了一系列改进的输入/输出的新特性，被统称为 `NIO`（即 `NewIO`），是同步非阻塞的。
2. `NIO` 相关类都被放在 `java.nio` 包及子包下，并且对原 `java.io` 包中的很多类进行改写。【基本案例】
3. `NIO` 有三大核心部分：**`Channel`（通道）、`Buffer`（缓冲区）、`Selector`（选择器）** 。
4. `NIO` 是**面向缓冲区，或者面向块编程**的。数据读取到一个它稍后处理的缓冲区，需要时可在缓冲区中前后移动，这就增加了处理过程中的灵活性，使用它可以提供非阻塞式的高伸缩性网络。
5. `Java NIO` 的非阻塞模式，使一个线程从某通道发送请求或者读取数据，但是它仅能得到目前可用的数据，如果目前没有数据可用时，就什么都不会获取，而不是保持线程阻塞，所以直至数据变的可以读取之前，该线程可以继续做其他的事情。非阻塞写也是如此，一个线程请求写入一些数据到某通道，但不需要等待它完全写入，这个线程同时可以去做别的事情。【后面有案例说明】
6. 通俗理解：`NIO` 是可以做到用一个线程来处理多个操作的。假设有 `10000` 个请求过来,根据实际情况，可以分配 `50` 或者 `100` 个线程来处理。不像之前的阻塞 `IO` 那样，非得分配 `10000` 个。
7. `HTTP 2.0` 使用了多路复用的技术，做到同一个连接并发处理多个请求，而且并发请求的数量比 `HTTP 1.1` 大了好几个数量级。

## 2. NIO和BIO的比较

1. `BIO` 以流的方式处理数据，而 `NIO` 以块的方式处理数据，块 `I/O` 的效率比流 `I/O` 高很多。

2. `BIO` 是阻塞的，`NIO` 则是非阻塞的。

3. `BIO` 基于字节流和字符流进行操作，而 `NIO` 基于 `Channel`（通道）和 `Buffer`（缓冲区）进行操作，数据总是从通道读取到缓冲区中，或者从缓冲区写入到通道中。`Selector`（选择器）用于监听多个通道的事件（比如：连接请求，数据到达等），因此使用单个线程就可以监听多个客户端通道。

4. Buffer和Channel之间的数据流向是双向的

   ![](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220202125522015.png)



## 3. NIO三大核心原理示意图

一张图描述 `NIO` 的 `Selector`、`Channel` 和 `Buffer` 的关系。

### Selector、Channel 和 Buffer 关系图（简单版）

关系图的说明:

![image-20220202125618828](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220202125618828.png)

1. 每个 `Channel` 都会对应一个 `Buffer`。
2. `Selector` 对应一个线程，一个线程对应多个 `Channel`（连接）。
3. 该图反应了有三个 `Channel` 注册到该 `Selector` //程序
4. 程序切换到哪个 `Channel` 是由事件决定的，`Event` 就是一个重要的概念。
5. `Selector` 会根据不同的事件，在各个通道上切换。
6. `Buffer` 就是一个内存块，底层是有一个数组。
7. 数据的读取写入是通过 `Buffer`，这个和 `BIO`是不同的，`BIO` 中要么是输入流，或者是输出流，不能双向，但是 `NIO` 的 `Buffer` 是可以读也可以写，需要 `flip` 方法切换 `Channel` 是双向的，可以返回底层操作系统的情况，比如 `Linux`，底层的操作系统通道就是双向的。

## 4. 缓冲区（Buffer）

缓冲区（`Buffer`）：缓冲区本质上是一个**可以读写数据的内存块**，可以理解成是一个**容器对象（含数组）**，该对象提供了一组方法，可以更轻松地使用内存块，缓冲区对象内置了一些机制，能够跟踪和记录缓冲区的状态变化情况。`Channel` 提供从文件、网络读取数据的渠道，但是读取或写入的数据都必须经由 `Buffer`，如图:【后面举例说明】

![image-20220202155225994](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220202155225994.png)

#### Buffer 类及其子类

1. 在 `NIO` 中，`Buffer` 是一个顶层父类，它是一个抽象类，类的层级关系图

   ![image-20220202155447306](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220202155447306.png)

2. `Buffer` 类定义了所有的缓冲区都具有的四个属性来提供关于其所包含的数据元素的信息：

   ![image-20220202155721434](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220202155721434.png)

   `Buffer` 类相关方法一览

   ![image-20220202171846001](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220202171846001.png)

#### ByteBuffer

从前面可以看出对于 `Java` 中的基本数据类型（`boolean` 除外），都有一个 `Buffer` 类型与之相对应，最常用的自然是 `ByteBuffer` 类（二进制数据），该类的主要方法如下：

![image-20220202172000108](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220202172000108.png)

## 5. 通道(Channel)

#### 基本介绍

1. `NIO`的通道类似于流，但有些区别如下：
   - 通道可以同时进行读写，而流只能读或者只能写
   - 通道可以实现异步读写数据
   - 通道可以从缓冲读数据，也可以写数据到缓冲:
   
2. `BIO` 中的 `Stream` 是单向的，例如 `FileInputStream` 对象只能进行读取数据的操作，而 `NIO` 中的通道（`Channel`）是双向的，可以读操作，也可以写操作。

3. `Channel` 在 `NIO` 中是一个接口 `public interface Channel extends Closeable{}`

4. 常用的 `Channel` 类有：**`FileChannel`、`DatagramChannel`、`ServerSocketChannel` 和 `SocketChannel`**。【`ServerSocketChanne` 类似 `ServerSocket`、`SocketChannel` 类似 `Socket`】

5. `FileChannel` 用于文件的数据读写，`DatagramChannel` 用于 `UDP` 的数据读写，`ServerSocketChannel` 和 `SocketChannel` 用于 `TCP` 的数据读写。

6. 图示

![image-20220203130805802](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220203130805802.png)

#### FileChannel 类

`FileChannel` 主要用来对本地文件进行 `IO` 操作，常见的方法有

- `public int read(ByteBuffer dst)`，从通道读取数据并放到缓冲区中
- `public int write(ByteBuffer src)`，把缓冲区的数据写到通道中
- `public long transferFrom(ReadableByteChannel src, long position, long count)`，从目标通道中复制数据到当前通道
- `public long transferTo(long position, long count, WritableByteChannel target)`，把数据从当前通道复制给目标通道

#### 应用实例1 - 本地文件写数据

实例要求：

1. 使用前面学习后的 `ByteBuffer`（缓冲）和 `FileChannel`（通道），将 "hello,尚硅谷" 写入到 `file01.txt` 中
2. 文件不存在就创建
3. 代码演示

```java
package com.fyp.nio;

import sun.nio.ByteBuffered;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description: 使用Channel写文件
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOFileChannel01 {
    public static void main(String[] args) throws IOException {
        String str = "hello,尚硅谷";
        //创建一个输出流 -> Channel
        FileOutputStream fileOutputStream = new FileOutputStream("d:\\file01.txt");

        //通过fileOutStream获取对应的 FileChannel
        //这个fileChannel 的真是类型是 FileChannelImpl
        FileChannel fileChannel = fileOutputStream.getChannel();

        //创建一个缓冲区 ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //将str放入byteBuffer
        byteBuffer.put(str.getBytes());

        //对byteBuffer 进行flip
        byteBuffer.flip();

        //将byteBuffer写入到 fileChannel
        fileChannel.write(byteBuffer);
        fileOutputStream.close();

    }
}

```

#### 应用实例2 - 本地文件读数据

实例要求：

1. 使用前面学习后的 `ByteBuffer`（缓冲）和 `FileChannel`（通道），将 `file01.txt` 中的数据读入到程序，并显示在控制台屏幕
2. 假定文件已经存在
3. 代码演示

```java
package com.fyp.nio;

import com.sun.xml.internal.fastinfoset.stax.events.AttributeBase;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description: 使用Channel读文件
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOFileChannel02 {
    public static void main(String[] args) throws IOException {

        //创建文件输入流
        File file = new File("d:\\file01.txt");
        FileInputStream fileInputStream = new FileInputStream(file);

        //通过fileInputStream获取对应的FileChannel -> 实际类型 FileChannelImpl
        FileChannel fileChannel = fileInputStream.getChannel();

        //创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());

        //将通道的数据读入到Buffer
        fileChannel.read(byteBuffer);

        //将 byteBuffer 的字节数据转成 String
        System.out.println(new String(byteBuffer.array()));
        fileInputStream.close();
    }
}

```

#### 应用实例3 - 使用一个 Buffer 完成文件读取、写入

实例要求：

1. 使用 `FileChannel`（通道）和方法 `read、write`，完成文件的拷贝
2. 拷贝一个文本文件 `1.txt`，放在项目下即可
3. 代码演示

![img](https://unpkg.zhimg.com/youthlql@1.0.0/netty/introduction/chapter_001/0016.png)

```java
package com.fyp.nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description: 使用Channel读写文件
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOFileChannel03 {
    public static void main(String[] args) throws IOException {
        FileInputStream fileInputStream = new FileInputStream("1.txt");
        FileChannel fileChannel01 = fileInputStream.getChannel();

        FileOutputStream fileOutputStream = new FileOutputStream("2.txt");
        FileChannel fileChannel02 = fileOutputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        
        while (true) { //循环读数

            /*
                public final Buffer clear() {
                    this.position = 0;
                    this.limit = this.capacity;
                    this.mark = -1;
                    return this;
                }
             */
            byteBuffer.clear();

            int read = fileChannel01.read(byteBuffer);
            System.out.println("read= " + read);
            if (read == -1) { //表示读完
                break;
            }
            //将buffer 中的数据 写入到 fileChannel02 --- 2.txt
            byteBuffer.flip();
            fileChannel02.write(byteBuffer);
        }

        fileInputStream.close();
    }
}

```

#### 应用实例4 - 拷贝文件 transferFrom 方法

1. 实例要求：
2. 使用 `FileChannel`（通道）和方法 `transferFrom`，完成文件的拷贝
3. 拷贝一张图片
4. 代码演示

```java
package com.fyp.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description: 拷贝文件-transferFrom方法
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOFileChannel04 {
    public static void main(String[] args) throws IOException {
        //创建输入流和输出流
        FileInputStream fileInputStream = new FileInputStream("d:\\wallhaven.png");
        FileOutputStream fileOutputStream = new FileOutputStream("d:\\wallhaven01.png");

        //获取各个流对应的Channel
        FileChannel sourceCh = fileInputStream.getChannel();
        FileChannel destCh = fileOutputStream.getChannel();

        //使用transferFrom完成拷贝
        destCh.transferFrom(sourceCh, 0, sourceCh.size());

        //关闭相关通道和流
        sourceCh.close();
        destCh.close();
        fileInputStream.close();
        fileOutputStream.close();
    }
}
```

#### 关于 Buffer 和 Channel 的注意事项和细节

1. `ByteBuffer` 支持类型化的 `put` 和 `get`，`put` 放入的是什么数据类型，`get` 就应该使用相应的数据类型来取出，否则可能有 `BufferUnderflowException` 异常。【举例说明】

```java
package com.fyp.nio;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description:
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOByteBufferPutGet {
    public static void main(String[] args) {
        //创建一个Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(64);

        //类型化操作放入数据
        byteBuffer.putInt(100);
        byteBuffer.putLong(9);
        byteBuffer.putChar('尚');
        byteBuffer.putShort((short) 4);

        //取出
        byteBuffer.flip();

        System.out.println();

        //按通道顺序获取,因为获取会移动position
        //而获取的数据是从position出发的，根据获取的get类型来定量position移动
        System.out.println(byteBuffer.getInt());
        System.out.println(byteBuffer.getLong());
        System.out.println(byteBuffer.getChar());
        System.out.println(byteBuffer.getShort());
    }
}

```

2. 可以将一个普通 `Buffer` 转成只读 `Buffer`【举例说明】

```java
package com.fyp.nio;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description:
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class ReadOnlyBuffer {
    public static void main(String[] args) {
        //创建一个Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(64);

        for (int i = 0; i < 64; i++) {
            byteBuffer.put((byte) i);
        }

        //读取
        byteBuffer.flip();

        //得到一个只读Buffer
        ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();
        System.out.println(readOnlyBuffer.getClass());

        //读取
        while (readOnlyBuffer.hasRemaining()) {
            System.out.println(readOnlyBuffer.get());
        }

        //会抛出 ReadOnlyBufferException 异常
        //readOnlyBuffer.put((byte) 100);
    }
}

```

3. `NIO` 还提供了 `MappedByteBuffer`，可以让文件直接在内存（堆外的内存）中进行修改，而如何同步到文件由 `NIO` 来完成。【举例说明】

```java
package com.fyp.nio;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/5
 * @Description: 直接修改内存
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
/*
说明：
1. MappedByteBuffer 可让文件直接在内存修改，操作系统不需要拷贝一次
 */
public class MappedByteBufferTest {
    public static void main(String[] args) throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile("1.txt", "rw");

        FileChannel channel = randomAccessFile.getChannel();

        /**
         * 参数1: FileChannel.MapMode.READ_WRITE 使用的读写模式
         * 参数2: 0: 可以直接修改的初始位置
         * 参数3: 5: 是映射到内存的大小，即将 1.txt 的多少个字节映射到内存
         * 可以直接修改的范围就是 0-5
         */
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

        mappedByteBuffer.put(0, (byte) 'H');
        mappedByteBuffer.put(3, (byte) '9');
        //会抛出 IndexOutOfBoundsException 异常
        //mappedByteBuffer.put(5, (byte) 'Y');

        randomAccessFile.close();


    }
}
```

4. 前面我们讲的读写操作，都是通过一个 `Buffer` 完成的，`NIO` 还支持通过多个 `Buffer`（即 `Buffer`数组）完成读写操作，即 `Scattering` 和 `Gathering`【举例说明】

```java
package com.fyp.nio;

/**
 * @Auther: fyp
 * @Date: 2022/2/5
 * @Description: Scatter和Gather的使用
 * @Package: com.fyp.nio
 * @Version: 1.0
 */

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Scattering: 将数据写入到buffer时，可以 采用buffer数组，依次写入[分散]
 * Gathering: 从buffer读取数据时，可以 采用buffer数组，依次读
 */
public class ScatteringAndGatheringTest {
    public static void main(String[] args) throws Exception{

        //使用ServerSocketChannel 和 SocketChannel 网络
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(7000);

        //绑定端口到socket, 并启动
        serverSocketChannel.socket().bind(inetSocketAddress);

        ByteBuffer[] byteBuffers = new ByteBuffer[2];
        byteBuffers[0] = ByteBuffer.allocate(5);
        byteBuffers[1] = ByteBuffer.allocate(3);

        //等待客户端连接 (telnet)
        SocketChannel socketChannel = serverSocketChannel.accept();
        int messageLength = 8;

        //循环地读数
        while (true) {
            int byteRead = 0;

            while (byteRead < messageLength) {
                long l = socketChannel.read(byteBuffers);
                byteRead += l;//累计读取的字节数
                System.out.println("byteRead= " + byteRead);
                //使用流打印，查看当前的这个buffer的position 和 limit
                Arrays.asList(byteBuffers).stream().map(buffer -> "position= " +
                        buffer.position() + ",limit= " + buffer.limit()).forEach(System.out::println);
            }

            //将所有的buffer 进行 flip
            Arrays.asList(byteBuffers).forEach(buffer -> buffer.flip());

            //将数据读出显示到客户端
            long byteWrite = 0;
            while (byteWrite < messageLength) {
                long l = socketChannel.write(byteBuffers);
                byteWrite += 1;
            }

            //将所有的 buffer 进行 clear
            Arrays.asList(byteBuffers).forEach(buffer -> {
                buffer.clear();
            });

            System.out.println("byteRead= " + byteRead + ",byteWrite= " + byteWrite + ",messageLength= " + messageLength);
        }
    }
}
```

## 6. 选择器（Selector）

#### 基本介绍

1. `Java` 的 `NIO`，用非阻塞的 `IO` 方式。可以用一个线程，处理多个的客户端连接，就会使用到 `Selector`（选择器）。
2. `Selector` 能够检测多个注册的通道上是否有事件发生（注意：多个 `Channel` 以事件的方式可以注册到同一个 `Selector`），如果有事件发生，便获取事件然后针对每个事件进行相应的处理。这样就可以只用一个单线程去管理多个通道，也就是管理多个连接和请求。
3. 只有在连接/通道真正有读写事件发生时，才会进行读写，就大大地减少了系统开销，并且不必为每个连接都创建一个线程，不用去维护多个线程。
4. 避免了多线程之间的上下文切换导致的开销。

#### Selector 示意图和特点说明

![image-20220205121619414](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220205121619414.png)

说明如下：

1. `Netty` 的 `IO` 线程 `NioEventLoop` 聚合了 `Selector`（选择器，也叫多路复用器），可以同时并发处理成百上千个客户端连接。
2. 当线程从某客户端 `Socket` 通道进行读写数据时，若没有数据可用时，该线程可以进行其他任务。
3. 线程通常将非阻塞 `IO` 的空闲时间用于在其他通道上执行 `IO` 操作，所以单独的线程可以管理多个输入和输出通道。
4. 由于读写操作都是非阻塞的，这就可以充分提升 `IO` 线程的运行效率，避免由于频繁 `I/O` 阻塞导致的线程挂起。
5. 一个 `I/O` 线程可以并发处理 `N` 个客户端连接和读写操作，这从根本上解决了传统同步阻塞 `I/O` 一连接一线程模型，架构的性能、弹性伸缩能力和可靠性都得到了极大的提升。

#### Selector 类相关方法

![image-20220205130354112](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220205130354112.png)

#### 注意事项

1. `NIO` 中的 `ServerSocketChannel` 功能类似 `ServerSocket`、`SocketChannel` 功能类似 `Socket`。
2. `Selector`相关方法说明
   - `selector.select();` //阻塞
   - `selector.select(1000);` //阻塞 1000 毫秒，在 1000 毫秒后返回
   - `selector.wakeup();` //唤醒 selector
   - `selector.selectNow();` //不阻塞，立马返还

## 7. NIO非阻塞网络编程原理分析图

`NIO` 非阻塞网络编程相关的（`Selector`、`SelectionKey`、`ServerScoketChannel` 和 `SocketChannel`）关系梳理图

![image-20220205140604693](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220205140604693.png)

对上图的说明：

1. 当客户端连接时，会通过 `ServerSocketChannel` 得到 `SocketChannel`。
2. `Selector` 进行监听 `select` 方法，返回有事件发生的通道的个数。
3. 将 `socketChannel` 注册到 `Selector` 上，`register(Selector sel, int ops)`，一个 `Selector` 上可以注册多个 `SocketChannel`。
4. 注册后返回一个 `SelectionKey`，会和该 `Selector` 关联（集合）。
5. 进一步得到各个 `SelectionKey`（有事件发生）。
6. 在通过 `SelectionKey` 反向获取 `SocketChannel`，方法 `channel()`。
7. 可以通过得到的 `channel`，完成业务处理。
8. 直接看后面代码吧

## 8. NIO非阻塞网络编程快速入门

案例：

1. 编写一个 `NIO` 入门案例，实现服务器端和客户端之间的数据简单通讯（非阻塞）
2. 目的：理解 `NIO` 非阻塞网络编程机制

服务端：

```java
package com.fyp.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @Auther: fyp
 * @Date: 2022/2/5
 * @Description: NIO服务器
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        //创建ServerSocketChannel -> ServerSocket
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //得到一个Selector对象
        Selector selector = Selector.open();

        //绑定一个端口6666，在服务端监听
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));

        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);

        //把 serverSocketChannel 注册到 selector, 关注事件 为 OP_ACCEPT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //循环等待客户端连接
        while (true) {
            //这里我们等待1秒，如果没有事件发生（连接事件）
            if (selector.select(1000) == 0) {//没有事件发生
                System.out.println("服务器等待了1秒，无连接");
                continue;
            }

            //如果返回的 > 0, 就获取到相关的 selectionKey集合
            //1. 如果返回的 > 0, 表示已经获取到关注的事件
            //2. selector.selectedKeys() 返回关注事件的集合
            //通过 selectionKeys 反向获取通道
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            //遍历 Set<SelectionKey>, 使用迭代器
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()) {
                //获取到selectionKey
                SelectionKey key = keyIterator.next();
                //根据key 对应的通道发生的事件做相应处理
                if (key.isAcceptable()) { //如果是OP_ACCEPT, 表示新的客户端连接
                    //给该客户端生成一个 SocketChannel
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("客户端连接成功！生成了一个socketChannel " + socketChannel.hashCode());
                    //将 SocketChannel 设置为非阻塞
                    socketChannel.configureBlocking(false);
                    //将socketChannel注册到 selector上, 关注事件为OP_READ, 同时给socketChannel
                    //关联一个Buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                if (key.isReadable()) { //发生OP_READ
                    // 通过key 反向获取对应的channel
                    SocketChannel channel = (SocketChannel) key.channel();
                    //获取该channel关联的 buffer,在与客户端连接就已经创建好了
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    channel.read(buffer);
                    System.out.println("from 客户端： " + new String(buffer.array()));
                }
                //手动从集合中移动当前的selectionKey, 防止重复操作
                keyIterator.remove();
            }

        }
    }
}
```
客户端：
```java
package com.fyp.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/6
 * @Description: 客户端
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOClient {
    public static void main(String[] args) throws IOException {
        //得到一个网络通道
        SocketChannel socketChannel = SocketChannel.open();
        //设置非阻塞
        socketChannel.configureBlocking(false);
        //提供服务端的 ip 和 端口
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);
        /*
            连接服务器
            1. 为非阻塞模式时，即不会等到方法执行完毕再返回，会立即返回，如果返回前已经连接成功，则返回true
            返回false 时，说明未连接成功，所以需要再通过while循环地finishConnect()完成最终的连接
            2. 为阻塞模式时，直到连接建立或抛出异常
            不会返回false，连接不上就抛异常，不需要借助finishConnect()
         */
        if (!socketChannel.connect(inetSocketAddress)) {
            while (!socketChannel.finishConnect()) {
                System.out.println("因为连接需要时间，客户端不会阻塞，可以做其他工作");
            }
        }
        //如果连接成功，就发送数据
        String str = "hello, 尚硅谷";
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());
        //发送数据，将buffer 写入 channel
        socketChannel.write(buffer);
        System.in.read();
    }
}
```

#### SelectionKey

1. `SelectionKey`，表示`Selector`和网络通道的注册关系，共四种：
   
   - `int OP_ACCEPT`：有新的网络连接可以 `accept`，值为 `16`
   - `int OP_CONNECT`：代表连接已经建立，值为 `8`
   - `int OP_READ`：代表读操作，值为 `1`
   - `int OP_WRITE`：代表写操作，值为 `4`
   
   源码中：

   ```java
   public static final int OP_READ = 1 << 0;
   public static final int OP_WRITE = 1 << 2;
   public static final int OP_CONNECT = 1 << 3;
   public static final int OP_ACCEPT = 1 << 4;
   ```

1. `SelectionKey` 相关方法

![image-20220206151712949](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220206151712949.png)

#### ServerSocketChannel

1. `ServerSocketChannel` 在服务器端监听新的客户端 `Socket` 连接，负责监听，不负责实际的读写操作
2. 相关方法如下

![image-20220206152107883](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220206152107883.png)

#### SocketChannel

1. `SocketChannel`，网络 `IO` 通道，**具体负责进行读写操作**。`NIO` 把缓冲区的数据写入通道，或者把通道里的数据读到缓冲区。
2. 相关方法如下

![image-20220206163247254](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220206163247254.png)

## 9. NIO网络编程应用实例-群聊系统

实例要求：

1. 编写一个 `NIO` 群聊系统，实现服务器端和客户端之间的数据简单通讯（非阻塞）
2. 实现多人群聊
3. 服务器端：可以监测用户上线，离线，并实现消息转发功能
4. 客户端：通过 `Channel` 可以无阻塞发送消息给其它所有用户，同时可以接受其它用户发送的消息（由服务器转发得到）
5. 目的：进一步理解 `NIO` 非阻塞网络编程机制
6. 示意图分析和代码

![image-20220207210932222](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220207210932222.png)

服务端：

```java
package com.fyp.nio.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @Auther: fyp
 * @Date: 2022/2/6
 * @Description: 群聊系统服务端
 * @Package: com.fyp.nio.groupchat
 * @Version: 1.0
 */
public class GroupChatServer {

    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final int PORT = 6667;

    public GroupChatServer() {

        try {
            selector = Selector.open();
            listenChannel = ServerSocketChannel.open();
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            listenChannel.configureBlocking(false);
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void listen() {
        try {
            while (true) {
                // count 获取的是 在阻塞过程中 同时发生的 事件 数，直到有事件 发生，才会执行，否则一直阻塞
                /*
                    select 方法在 没有 客户端发起连接时， 会一直阻塞，至少有一个客户端连接，其他 客户端再 发起连接 不再阻塞，会立即返回
                 */
                int count = selector.select();
                System.out.println(count);
                if(count > 0) {// 有事件 处理
                    // 遍历得到 SelectionKey 集合
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        //取出SelectionKey
                        SelectionKey key = iterator.next();
                        //监听到accept
                        if (key.isAcceptable()) {
                            SocketChannel sc = listenChannel.accept();
                            // 监听到 客户端 的 SocketChannel 总是默认为阻塞方式，需要重新设置
                            sc.configureBlocking(false);
                            //将该 sc 注册到 selector
                            sc.register(selector, SelectionKey.OP_READ);

                            System.out.println(sc.getRemoteAddress() + " 上线 ");
                        }

                        if (key.isReadable()) { // 通道发送 read 事件， 即通道是可读状态
                            //处理读
                            readData(key);
                        }
                        /*
                            每次  监听到 客户端后， selector会将 连接上的 客户端 选中， 并添加到 selectionKeys 中
                            要注册到 selector 上，使用该方法，selector 将 不再选中
                            如果没有移除，selector 不能选中 其他的 客户端连接
                            iterator.remove() 移除后，将释放 selector 中的 selectionKeys
                         */
                        iterator.remove();
                    }
                } else {
                    //System.out.println("等待....");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 发送异常处理
        }
    }

    public void readData(SelectionKey key) {
        // 取到关联的 channel
        SocketChannel channel = null;
        try {
            // 得到 channel
             channel = (SocketChannel) key.channel();

             // 创建buffer
             ByteBuffer buffer = ByteBuffer.allocate(1024);

             int count = channel.read(buffer);

             // 根据 count 的值 做 处理
            if (count > 0) {
                // 把 缓冲区 的 数据 转成 字符串
                String msg = new String(buffer.array());
                // 输出该消息
                System.out.println("from 客户端： " + msg);
                //想其他客户端转发消息，专门写一个方法来处理
                sendInfoToOtherClients(msg, channel);
            }

        } catch (IOException e) {
            //e.printStackTrace();
            try {
                System.out.println(channel.getRemoteAddress() + "离线了");
                // 取消 注册
                key.cancel();
                // 关闭通道
                channel.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // 转发消息给其他客户端（通道）
    private void sendInfoToOtherClients(String msg, SocketChannel self) throws IOException {
        System.out.println("服务器转发消息中....");
        // 遍历所有 注册到 selector 上 的 SockChannel, 并排除 self
        for (SelectionKey key : selector.keys()) {
            // 通过 key 取出 对应的 SocketChannel
            Channel targetChannel = key.channel();

            // 排除自己
            if (targetChannel instanceof SocketChannel && targetChannel != self) {
                // 转型
                SocketChannel dest = (SocketChannel) targetChannel;
                // 将 msg 存储到 buffer
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                // 将 buffer 的数据 写入 通道
                dest.write(buffer);
            }

        }
    }


    public static void main(String[] args) {
        // 创建 服务器 对象
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.listen();
    }
}
```

客户端：

```java
package com.fyp.nio.groupchat;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @Auther: fyp
 * @Date: 2022/2/7
 * @Description: 群聊系统客户端
 * @Package: com.fyp.nio.groupchat
 * @Version: 1.0
 */
public class GroupChatClient {

    //定义 相关 属性
    private final String HOST = "127.0.0.1";
    private final int PORT = 6667;
    private Selector selector;
    private SocketChannel socketChannel;
    private String username;

    // 构造器，完成初始化工作
    public GroupChatClient() throws IOException {

        selector = Selector.open();
        // 连接 服务器
        socketChannel = socketChannel.open(new InetSocketAddress("127.0.0.1", PORT));
        // 设置 非阻塞
        socketChannel.configureBlocking(false);
        // 将 channel 注册到 selector
        socketChannel.register(selector, SelectionKey.OP_READ);
        // 得到 username
        username = socketChannel.getLocalAddress().toString().substring(1);
        System.out.println(username + " is ok....");

    }

    // 向 服务器 发送 消息
    public void sendInfo(String info) {

        info = username + " 说：" + info;

        try {
            socketChannel.write(ByteBuffer.wrap(info.getBytes()));

        } catch (Exception e) {

        }
    }

    // 读取从 服务器 端 回复的 消息
    public void readInfo() {
        try {
            int readChannels = selector.select();
            if (readChannels > 0) {// 有可以用的 通道

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {

                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        // 得到 相关的 通道
                        SocketChannel sc = (SocketChannel) key.channel();
                        // 得到一个 Buffer
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        // 读取
                        sc.read(buffer);
                        // 把缓存区的数据 转成 字符串
                        String msg = new String(buffer.array());
                        System.out.println(msg.trim());
                    }
                }
                iterator.remove();// 删除当前的selectionKey, 防止重复操作, 没有删除，直接影响到selector.select()
            } else {
                //System.out.println("没有可以用的通道....");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // 启动 客户端
        GroupChatClient chatClient = new GroupChatClient();
        // 启动一个线程，每隔3秒， 读取从 服务器 发送过来的数据
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    chatClient.readInfo();

                    try {
                        Thread.currentThread().sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        // 发送数据给 服务端
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            chatClient.sendInfo(s);
        }

    }

}
```

## 10. NIO与零拷贝

### 零拷贝之传统文件IO

场景：将磁盘上的文件读取出来，然后通过网络协议发送给客户端。

![image-20220208151651241](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220208151651241.png)

很明显发生了4次拷贝

- 第一次拷贝，把磁盘上的数据拷贝到操作系统内核的缓冲区里，这个拷贝是通过 DMA 的。
- 第二次拷贝，把内核缓冲区的数据拷贝到用户的缓冲区里，于是应用程序就可以使用这部分数据了，这个拷贝是由 CPU 完成的。
- 第三次拷贝，把刚才拷贝到用户的缓冲区里的数据，再拷贝到内核的 socket 的缓冲区里，这个过程依然由 CPU 完成的。
- 第四次拷贝，把内核的 socket 缓冲区里的数据，拷贝到协议栈里，这个过程又是由 DMA 完成的。
  发生了4次用户上下文切换，因为发生了两个系统调用read和write。一个系统调用对应两次上下文切换，所以上下文切换次数在一般情况下只可能是偶数。

>  想要优化文件传输的性能就两个方向

- 减少上下文切换次数
- 减少数据拷贝次数

因为这两个是最耗时的

### 零拷贝之mmap

read() 系统调用的过程中会把内核缓冲区的数据拷贝到用户的缓冲区里，为了减少这一步开销，我们可以用 mmap() 替换 read() 系统调用函数。mmap() 系统调用函数会直接把内核缓冲区里的数据映射到用户空间，这样，操作系统内核与用户空间共享缓冲区，就不需要再进行任何的数据拷贝操作。


![image-20220208151951444](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220208151951444.png)

总的来说mmap减少了一次数据拷贝，总共4次上下文切换，3次数据拷贝

### 零拷贝之sendfile

`Linux2.1` 版本提供了 `sendFile` 函数，其基本原理如下：数据根本不经过用户态，直接从内核缓冲区进入到 `SocketBuffer`


![image-20220208152402596](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220208152402596.png)

总的来说有2次上下文切换，3次数据拷贝。

### sendfile再优化

`Linux在2.4` 版本中，做了一些修改，避免了从内核缓冲区拷贝到 `Socketbuffer` 的操作，直接拷贝到协议栈，从而再一次减少了数据拷贝

![image-20220208152535164](https://yupeng-tuchuang.oss-cn-shenzhen.aliyuncs.com/image-20220208152535164.png)

### mmap 和 sendFile 的区别

（1）`mmap` 适合小数据量读写，`sendFile` 适合大文件传输。

（2）`mmap` 需要 `4` 次上下文切换，`3` 次数据拷贝；`sendFile` 需要 `3` 次上下文切换，最少 `2` 次数据拷贝。

（3）`sendFile` 可以利用 `DMA` 方式，减少 `CPU` 拷贝，`mmap` 则不能（必须从内核拷贝到 `Socket`缓冲区）。

### NIO 零拷贝案例

案例要求：

1. 使用传统的 `IO` 方法传递一个大文件
2. 使用 `NIO` 零拷贝方式传递（`transferTo`）一个大文件
3. 看看两种传递方式耗时时间分别是多少

#### 传统拷贝方式

服务端：

```java
package com.fyp.nio.zerocopy;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Auther: fyp
 * @Date: 2022/2/9
 * @Description: 传统IO服务端
 * @Package: com.fyp.nio.zerocopy
 * @Version: 1.0
 */
public class OldIOServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(7001);

        while (true) {
            Socket socket = serverSocket.accept();
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            try {

                byte[] byteArray = new byte[4096];

                while (true) {
                    int readCount = dataInputStream.read(byteArray, 0, byteArray.length);

                    if (-1 == readCount) {
                        break;
                    }

                    System.out.println("读取字节数： " + readCount);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
```

客户端：

```java
package com.fyp.nio.zerocopy;



import java.io.*;
import java.net.Socket;

/**
 * @Auther: fyp
 * @Date: 2022/2/9
 * @Description: 传统IO客户端
 * @Package: com.fyp.nio.zerocopy
 * @Version: 1.0
 */
public class OldIOClient {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("localhost", 7001);

        String fileName = "aiXcoder-3.3.1-2020.zip";
        InputStream inputStream = new FileInputStream(fileName);

        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        byte[] buffer = new byte[4096];
        long readCount;
        long total = 0;

        long startTime = System.currentTimeMillis();

        while ((readCount = inputStream.read(buffer)) > 0) {
            total += readCount;
            dataOutputStream.write(buffer, 0 , (int) readCount);
        }

        System.out.println("发送总字节数： " + total + ", 耗时： " + (System.currentTimeMillis() - startTime));


        dataOutputStream.close();
        socket.close();
        inputStream.close();
    }

}
```

#### NIO拷贝方式

服务端：

```java
package com.fyp.nio.zerocopy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/9
 * @Description: 新IO服务端
 * @Package: com.fyp.nio.zerocopy
 * @Version: 1.0
 */
public class NewIOServer {

    public static void main(String[] args) throws IOException {

        InetSocketAddress inetSocketAddress = new InetSocketAddress(7001);

        ServerSocketChannel serverSockChannel = ServerSocketChannel.open();

        ServerSocket serverSocket = serverSockChannel.socket();

        serverSocket.bind(inetSocketAddress);

        // 创建 buffer
        ByteBuffer buffer = ByteBuffer.allocate(4096);

        while (true) {
             SocketChannel socketChannel = serverSockChannel.accept();

             int readCount = 0;
             while (-1 != readCount) {
                 try {
                     readCount = socketChannel.read(buffer);

                 } catch (Exception e) {
                     //e.printStackTrace();
                     break;
                 }
                 //
                 buffer.rewind(); // 倒带 position = 0 , mark 作废
             }
        }
    }

}
```

客户端：

```java
package com.fyp.nio.zerocopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/9
 * @Description: 新IO客户端
 * @Package: com.fyp.nio.zerocopy
 * @Version: 1.0
 */
public class NewIOClient {

    public static void main(String[] args) throws IOException {

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 7001));
        String filename = "aiXcoder-3.3.1-2020.zip";

        // 得到 一个 文件 channel
        FileChannel fileChannel = new FileInputStream(filename).getChannel();

        // 准备 发送
        long startTime = System.currentTimeMillis();

        // 在 linux 的 下一个 transferTo 方法就可以 完成 传输
        // 在 windows 下一次 调用 transferTo 只能发送 8M
        // 需要 分段 传输文件，而且 主要 传输时 的位置 transferTo 底层用到零拷贝
        long transferCount = fileChannel.transferTo(0, fileChannel.size(), socketChannel);

        System.out.println("发送总字节数： " + transferCount + ", 耗时： " + (System.currentTimeMillis() - startTime));

        fileChannel.close();
    }

}
```

# 三、Java AIO介绍

##  1. AIO基本介绍

1. `JDK7` 引入了 `AsynchronousI/O`，即 `AIO`。在进行 `I/O` 编程中，常用到两种模式：`Reactor` 和 `Proactor`。`Java` 的 `NIO` 就是 `Reactor`，当有事件触发时，服务器端得到通知，进行相应的处理
2. `AIO` 即 `NIO2.0`，叫做异步不阻塞的 `IO`。`AIO` 引入异步通道的概念，采用了 `Proactor` 模式，简化了程序编写，有效的请求才启动线程，它的特点是先由操作系统完成后才通知服务端程序启动线程去处理，一般适用于连接数较多且连接时间较长的应用
3. 目前 `AIO` 还没有广泛应用，`Netty` 也是基于 `NIO`，而不是 `AIO`，因此我们就不详解 `AIO` 了，有兴趣的同学可以参考[《Java新一代网络编程模型AIO原理及Linux系统AIO介绍》](http://www.52im.net/thread-306-1-1.html)

## 2. BIO、NIO、AIO对比表

|          | BIO      | NIO                    | AIO        |
| -------- | -------- | ---------------------- | ---------- |
| IO模型   | 同步阻塞 | 同步非阻塞（多路复用） | 异步非阻塞 |
| 编程难度 | 简单     | 复杂                   | 复杂       |
| 可靠性   | 差       | 好                     | 好         |
| 吞吐量   | 低       | 高                     | 高         |

**举例说明**

1. 同步阻塞：到理发店理发，就一直等理发师，直到轮到自己理发。
2. 同步非阻塞：到理发店理发，发现前面有其它人理发，给理发师说下，先干其他事情，一会过来看是否轮到自己.
3. 异步非阻塞：给理发师打电话，让理发师上门服务，自己干其它事情，理发师自己来家给你理发

[ 漫画讲IO](https://mp.weixin.qq.com/s?__biz=Mzg3MjA4MTExMw==&mid=2247484746&idx=1&sn=c0a7f9129d780786cabfcac0a8aa6bb7&source=41&scene=21#wechat_redirect)