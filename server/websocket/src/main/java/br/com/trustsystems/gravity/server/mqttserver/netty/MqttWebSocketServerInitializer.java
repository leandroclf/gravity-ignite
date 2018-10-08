package br.com.trustsystems.gravity.server.mqttserver.netty;

import br.com.trustsystems.gravity.server.mqttserver.codec.MqttWebSocketCodec;
import br.com.trustsystems.gravity.server.netty.SSLHandler;
import br.com.trustsystems.gravity.server.netty.ServerImpl;
import br.com.trustsystems.gravity.server.netty.ServerInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;

public class MqttWebSocketServerInitializer extends ServerInitializer<MqttMessage> {


    public MqttWebSocketServerInitializer(ServerImpl<MqttMessage> serverImpl, int connectionTimeout) {
        super(serverImpl, connectionTimeout);
    }

    public MqttWebSocketServerInitializer(ServerImpl<MqttMessage> serverImpl, int connectionTimeout, SSLHandler sslHandler) {
        super(serverImpl, connectionTimeout, sslHandler);
    }

    @Override
    protected void customizePipeline(ChannelPipeline pipeline) {
        String websocketPath = "/mqtt";

        pipeline.addLast(HttpServerCodec.class.getName(), new HttpServerCodec());
        pipeline.addLast(HttpObjectAggregator.class.getName(), new HttpObjectAggregator(1048576));
        pipeline.addLast(HttpContentCompressor.class.getName(), new HttpContentCompressor());
        pipeline.addLast(WebSocketServerProtocolHandler.class.getName(),
                new WebSocketServerProtocolHandler(websocketPath, "mqtt,mqttv3.1,mqttv3.1.1", true, 1048576));
        pipeline.addLast(new MqttWebSocketCodec());
        pipeline.addLast("decoder", new MqttDecoder(1048576));
        pipeline.addLast("encoder", new MqttEncoder());

        // we finally have the chance to add some business logic.
        pipeline.addLast(new MqttSocketServerHandler((MqttSocketServerImpl) getServerImpl()));
    }


}
