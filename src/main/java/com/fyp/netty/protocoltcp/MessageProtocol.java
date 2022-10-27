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
