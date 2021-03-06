package com.newfly.controller;

import com.newfly.common.SocketChannelMap;
import com.newfly.pojo.ResultMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class SocketServerHandler extends SimpleChannelInboundHandler<Object>
{
    private static final Logger logger = LoggerFactory.getLogger(NewFlyServer.class);

    private final GameController gameController;
    private final LoginController loginController;
    private final BattleController battleController;

    public SocketServerHandler(GameController gameController, LoginController loginController, BattleController battleController) {
        this.gameController = gameController;
        this.loginController = loginController;
        this.battleController = battleController;
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        logger.debug("连接异常: 客户端错误的断开了连接");
        ctx.close();
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

}// end
