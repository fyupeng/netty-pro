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
