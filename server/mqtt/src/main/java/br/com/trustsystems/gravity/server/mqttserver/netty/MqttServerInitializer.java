package br.com.trustsystems.gravity.server.mqttserver.netty;

import br.com.trustsystems.gravity.server.netty.SSLHandler;
import br.com.trustsystems.gravity.server.netty.ServerImpl;
import br.com.trustsystems.gravity.server.netty.ServerInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;

public class MqttServerInitializer extends ServerInitializer<MqttMessage> {


    public MqttServerInitializer(ServerImpl<MqttMessage> serverImpl, int connectionTimeout) {
        super(serverImpl, connectionTimeout);
    }

    public MqttServerInitializer(ServerImpl<MqttMessage> serverImpl, int connectionTimeout, SSLHandler sslHandler) {
        super(serverImpl, connectionTimeout, sslHandler);
    }

    @Override
    protected void customizePipeline(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", new MqttDecoder(1048576));
        pipeline.addLast("encoder", new MqttEncoder());

        // we finally have the chance to add some business logic.
        pipeline.addLast( new MqttServerHandler((MqttServerImpl) getServerImpl()));
    }


}
