package br.com.trustsystems.gravity.server.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class TimeoutHandler extends ChannelHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleState e = ((IdleStateEvent) evt).state();
            if (e == IdleState.ALL_IDLE) {
                ctx.fireChannelInactive();
                ctx.close();
            }
        }
    }
}
