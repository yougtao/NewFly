package com.newfly.controller;

import com.newfly.common.SocketChannelMap;
import com.newfly.pojo.ResultMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SocketServerHandler extends SimpleChannelInboundHandler<Object>
{
    private static final Logger logger = LoggerFactory.getLogger(NewFlyServer.class);

    // 初始化时获取
    private static GameController gameController;
    private static LoginController loginController;
    private static BattleController battleController;


    static {
        ApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
        gameController = (GameController) context.getBean("gameController");
        loginController = (LoginController) context.getBean("loginController");
        battleController = (BattleController) context.getBean("battleController");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        logger.debug("异常发生: 捕获于SocketServerHandler:exceptionCaught()", throwable);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) {
        ResultMessage msg = (ResultMessage) o;
        logger.info("收到消息 type=" + msg.getType() + " body=>" + msg.getBody());
        // logger.info("Controller:" + serverController);

        // 将处理分到不同的Controller上
        int type = msg.getType();
        ResultMessage result;
        // 判断用哪个controller处理
        if (type / 1000 == 3) {
            result = loginController.handle(ctx, msg);
        } else if (type / 1000 == 6) {
            result = battleController.handle(ctx, msg);
        } else {
            result = gameController.handle(ctx, msg);
        }

        if (result != null)
            ctx.writeAndFlush(result);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("与新的客户端建立连接");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("与客户端连接断开");

        // 异常退出
        loginController.close(ctx);
        SocketChannelMap.remove((SocketChannel) ctx.channel()); // 删除channel
        super.channelInactive(ctx);
    }
}
