package com.newfly.common;

import com.newfly.pojo.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

public class LengthFieldMessageEncoder extends MessageToByteEncoder<Message>
{
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        if (message == null) {
            throw new Exception("消息内容为空!");
        }

        String msgBody = message.getBody();
        byte[] b = msgBody.getBytes(Charset.forName("utf-8"));
        byteBuf.writeShort(b.length);
        byteBuf.writeShort(message.getType());
        byteBuf.writeBytes(b);
    }
}
