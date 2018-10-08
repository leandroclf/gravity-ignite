package br.com.trustsystems.gravity.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

public abstract class ServerInitializer<T> extends ChannelInitializer<SocketChannel> {


    private final int connectionTimeout;
    private final SSLHandler sslHandler;
    private final ServerImpl serverImpl;

    public ServerInitializer(ServerImpl serverImpl, int connectionTimeout, SSLHandler sslHandler) {
        this.serverImpl = serverImpl;
        this.sslHandler = sslHandler;
        this.connectionTimeout = connectionTimeout;
    }

    public ServerInitializer(ServerImpl serverImpl, int connectionTimeout) {
        this.serverImpl = serverImpl;
        this.sslHandler = null;
        this.connectionTimeout = connectionTimeout;
    }

    public ServerImpl<T> getServerImpl() {
        return serverImpl;
    }


    public SSLHandler getSslHandler() {
        return sslHandler;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * This method will be called once the {@link SocketChannel} was registered. After the method returns this instance
     * will be removed from the {@link ChannelPipeline} of the {@link SocketChannel}.
     *
     * @param ch the {@link SocketChannel} which was registered.
     * @throws Exception is thrown if an error occurs. In that case the {@link SocketChannel} will be closed.
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        if (null != getSslHandler()) {
            // Add SSL handler first to encrypt and decrypt everything.
            // In this application ssl is only used for transport encryption
            // Identification is not yet part of the deal.

            pipeline.addLast("ssl", new SslHandler(getSslHandler().getSSLEngine()));
        }


        customizePipeline(pipeline);

    }

    protected abstract void customizePipeline(ChannelPipeline pipeline);


}
