package com.newfly.test;


import com.newfly.common.LengthFieldMessageDecoder;
import com.newfly.common.LengthFieldMessageEncoder;
import com.newfly.pojo.ResultMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class SocketClientTest
{
    private static final Logger logger = LoggerFactory.getLogger(SocketClientTest.class);
    private static final String IP = "127.0.0.1";
    //private static final String IP = "172.30.192.163";
    //private static final String IP = "172.18.12.67";
    private static final int PORT = 9600;

    private static EventLoopGroup group = new NioEventLoopGroup();

    @SuppressWarnings("rawtypes")
    private static void run() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer()
        {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                /*
                 * 这个地方的 必须和服务端对应上。否则无法正常解码和编码
                 *
                 * 解码和编码 我将会在下一张为大家详细的讲解。再次暂时不做详细的描述
                 *
                 * */
                pipeline.addLast("framerDecoder", new LengthFieldMessageDecoder());
                pipeline.addLast("frameEncoder", new LengthFieldMessageEncoder());
                //pipeline.addLast("decoder", new StringDecoder());
                //pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast(new SocketClientHandlerTest());
            }
        });

        // 连接服务端
        ChannelFuture channelFuture = bootstrap.connect(IP, PORT).sync();

        // 模拟发包
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int type = 1000;
        String m;
        while (type > 0) {
            type = Integer.parseInt(br.readLine());
            m = br.readLine();
            ResultMessage msg = new ResultMessage(0, type, m);
            channelFuture.channel().writeAndFlush(msg);
            logger.info("已发送数据:" + msg.getType() + "=>" + msg.getBody());
        }
        channelFuture.channel().closeFuture().sync();

    }

    public static void main(String[] args) {
        logger.info("开始连接Socket服务器...");
        try {
            run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
