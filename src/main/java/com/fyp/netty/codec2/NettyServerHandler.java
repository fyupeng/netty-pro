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
