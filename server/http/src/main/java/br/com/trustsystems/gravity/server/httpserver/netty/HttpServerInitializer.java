package br.com.trustsystems.gravity.server.httpserver.netty;

import br.com.trustsystems.gravity.server.netty.SSLHandler;
import br.com.trustsystems.gravity.server.netty.ServerImpl;
import br.com.trustsystems.gravity.server.netty.ServerInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpServerInitializer extends ServerInitializer<FullHttpMessage> {


    public HttpServerInitializer(ServerImpl<FullHttpMessage> serverImpl, int connectionTimeout) {
        super(serverImpl, connectionTimeout);
    }


    public HttpServerInitializer(ServerImpl<FullHttpMessage> serverImpl, int connectionTimeout, SSLHandler sslHandler) {
        super(serverImpl, connectionTimeout, sslHandler);
    }

    @Override
    protected void customizePipeline(ChannelPipeline pipeline) {

        pipeline.addLast("server", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));

        // we finally have the chance to add some business logic.
        pipeline.addLast(new HttpServerHandler((HttpServerImpl) getServerImpl()));

    }

}
