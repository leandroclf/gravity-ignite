package br.com.trustsystems.gravity.server.netty;

import br.com.trustsystems.gravity.core.modules.Server;
import br.com.trustsystems.gravity.core.worker.state.messages.DisconnectMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.server.ServerInterface;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class ServerImpl<T> implements ServerInterface<T> {

    public static final AttributeKey<Serializable> REQUEST_SESSION_ID = AttributeKey.valueOf("requestSessionIdKey");
    public static final AttributeKey<Serializable> REQUEST_CONNECTION_ID = AttributeKey.valueOf("requestConnectionIdKey");
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final Server<T> internalServer;
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private int tcpPort;
    private int sslPort;
    private boolean sslEnabled;
    private int connectionTimeout;
    private SSLHandler sslHandler = null;
    private EventLoopGroup parentGroup = null;
    private EventLoopGroup childGroup = null;
    private Channel tcpChannel = null;
    private Channel sslChannel = null;

    public ServerImpl(Server<T> internalServer) {

        this.internalServer = internalServer;
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getSslPort() {
        return sslPort;
    }

    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public SSLHandler getSslHandler() {
        return sslHandler;
    }

    public void setSslHandler(SSLHandler sslHandler) {
        this.sslHandler = sslHandler;
    }

    public Server<T> getInternalServer() {
        return internalServer;
    }


    /**
     * The @link configure method is responsible for starting the implementation server processes.
     * The implementation should return once the server has started this allows
     * the launcher to maintain the life of the application.
     *
     * @throws UnRetriableException
     */
    public void initiate() throws UnRetriableException {

        log.info(" configure : initiating the netty server.");

        try {

            parentGroup = new NioEventLoopGroup(1);
            childGroup = new NioEventLoopGroup();


            //Initialize listener for TCP
            ServerBootstrap tcpBootstrap = new ServerBootstrap();
            tcpBootstrap.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(getServerInitializer(this, getConnectionTimeout()));

            ChannelFuture tcpChannelFuture = tcpBootstrap.bind(getTcpPort()).sync();
            tcpChannel = tcpChannelFuture.channel();


            if (isSslEnabled()) {
                //Initialize listener for SSL
                ServerBootstrap sslBootstrap = new ServerBootstrap();
                sslBootstrap.group(parentGroup, childGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(getServerInitializer(this, getConnectionTimeout(), getSslHandler()));

                ChannelFuture sslChannelFuture = sslBootstrap.bind(getSslPort()).sync();
                sslChannel = sslChannelFuture.channel();
            }

        } catch (InterruptedException e) {

            log.error(" configure : Initialization issues ", e);

            throw new UnRetriableException(e);

        }


    }


    /**
     * @link terminate method is expected to cleanly shut down the server implementation and return immediately.
     */
    public void terminate() {
        log.info(" terminate : stopping any processing. ");

        //Stop all connections.
        getChannelGroup().close().awaitUninterruptibly();


        if (null != sslChannel) {
            sslChannel.close().awaitUninterruptibly();
        }

        if (null != tcpChannel) {
            tcpChannel.close().awaitUninterruptibly();
        }

        if (null != childGroup) {
            childGroup.shutdownGracefully();
        }

        if (null != parentGroup) {
            parentGroup.shutdownGracefully();
        }

    }


    public void pushToClient(Serializable connectionId, T message) {

        log.debug(" pushToClient : Server pushToClient : we got to now sending out {}", message);

        Channel channel = getChannel((ChannelId) connectionId);

        if (null != channel && channel.isWritable()) {
            channel.writeAndFlush(message);
        } else {
            log.info(" pushToClient : channel to push message {} is not availble ", message);
        }

    }


    public void closeClient(ChannelId channelId) {
        Channel channel = getChannel(channelId);
        if (null != channel) {

            channel.attr(ServerImpl.REQUEST_SESSION_ID).set(null);
            channel.attr(ServerImpl.REQUEST_CONNECTION_ID).set(null);

            channel.close();
        }
    }


    @Override
    public void postProcess(IOTMessage ioTMessage) {

        if (ioTMessage.getMessageType().equals(DisconnectMessage.MESSAGE_TYPE)) {
            closeClient((ChannelId) ioTMessage.getConnectionId());
        }

    }

    protected Channel getChannel(ChannelId channelId) {
        return getChannelGroup().find(channelId);
    }

    protected abstract ServerInitializer<T> getServerInitializer(ServerImpl<T> serverImpl, int connectionTimeout);

    protected abstract ServerInitializer<T> getServerInitializer(ServerImpl<T> serverImpl, int connectionTimeout, SSLHandler sslHandler);

}
