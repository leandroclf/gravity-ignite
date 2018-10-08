package br.com.trustsystems.gravity.server.mqttserver.netty;

import br.com.trustsystems.gravity.server.netty.ServerHandler;
import br.com.trustsystems.gravity.server.netty.ServerImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;

import java.io.Serializable;

public class MqttSocketServerHandler extends ServerHandler<MqttMessage> {


    public MqttSocketServerHandler(MqttSocketServerImpl serverImpl) {
        super(serverImpl);
    }

    /**
     * Is called for each message of type {@link MqttMessage}.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     */
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, MqttMessage msg) {

        log.debug(" messageReceived : received the message {}", msg);

        Serializable connectionId = ctx.channel().attr(ServerImpl.REQUEST_CONNECTION_ID).get();
        Serializable sessionId = ctx.channel().attr(ServerImpl.REQUEST_SESSION_ID).get();

        getInternalServer().pushToWorker(connectionId, sessionId, msg);

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        try {
            log.info(" exceptionCaught : Unhandled exception: ", cause);

            getServerImpl().closeClient(ctx.channel().id());

        } catch (Exception ex) {
            log.debug(" exceptionCaught : trying to close socket because we got an unhandled exception", ex);
        }


    }
}
