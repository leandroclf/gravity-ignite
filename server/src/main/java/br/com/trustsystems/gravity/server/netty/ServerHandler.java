package br.com.trustsystems.gravity.server.netty;

import br.com.trustsystems.gravity.core.modules.Server;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class ServerHandler<T> extends SimpleChannelInboundHandler<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final ServerImpl<T> serverImpl;

    public ServerHandler(ServerImpl<T> serverImpl) {
        this.serverImpl = serverImpl;
    }

    public ServerImpl getServerImpl() {
        return serverImpl;
    }

    public Server getInternalServer() {
        return getServerImpl().getInternalServer();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        ChannelGroup channelGroup = getServerImpl().getChannelGroup();

        ctx.channel().attr(ServerImpl.REQUEST_CONNECTION_ID).set(channel.id());

        channelGroup.add(channel);
        super.channelActive(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        Serializable sessionId = ctx.channel().attr(ServerImpl.REQUEST_SESSION_ID).get();

        if (null != sessionId) {

            Serializable connectionId = ctx.channel().attr(ServerImpl.REQUEST_CONNECTION_ID).get();

            getInternalServer().dirtyDisconnect(connectionId, sessionId);

        }
    }


}
