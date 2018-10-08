package br.com.trustsystems.gravity.server.httpserver.netty;

import br.com.trustsystems.gravity.server.netty.ServerHandler;
import br.com.trustsystems.gravity.server.netty.ServerImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

import java.io.Serializable;

public class HttpServerHandler extends ServerHandler<FullHttpMessage> {


    public HttpServerHandler(HttpServerImpl serverImpl) {
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
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpMessage msg) {

        log.debug(" messageReceived : received the message {}", msg);

        Serializable connectionId = ctx.channel().attr(ServerImpl.REQUEST_CONNECTION_ID).get();

        getInternalServer().pushToWorker(connectionId, null, msg);

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        try {
            log.info(" exceptionCaught : Unhandled exception: ", cause);

            JSONObject error = new JSONObject();
            error.put("message", cause.getMessage());
            error.put("status", "failure");

            ByteBuf buffer = Unpooled.copiedBuffer(error.toString(), CharsetUtil.UTF_8);

            // Build the response object.
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    buffer);

            ctx.channel().writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception ex) {
            log.debug(" exceptionCaught : trying to close socket because we got an unhandled exception", ex);
        }


    }
}
