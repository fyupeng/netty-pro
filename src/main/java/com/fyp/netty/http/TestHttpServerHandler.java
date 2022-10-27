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


        System.out.println("对应的channel:" + ctx.channel() +  " 对应的pipeline: " + ctx.pipeline() +  " 通过pipeline获取channel:" + ctx.pipeline().channel());

        System.out.println("当前ctx的handler " + ctx.handler());

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
