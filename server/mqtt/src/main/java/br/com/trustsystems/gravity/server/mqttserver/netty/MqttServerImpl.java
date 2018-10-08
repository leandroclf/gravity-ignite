package br.com.trustsystems.gravity.server.mqttserver.netty;

import br.com.trustsystems.gravity.core.modules.Server;
import br.com.trustsystems.gravity.core.worker.state.messages.ConnectAcknowledgeMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.server.netty.SSLHandler;
import br.com.trustsystems.gravity.server.netty.ServerImpl;
import br.com.trustsystems.gravity.server.netty.ServerInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.apache.commons.configuration.Configuration;

public class MqttServerImpl extends ServerImpl<MqttMessage> {


    public static final String CONFIGURATION_SERVER_MQTT_TCP_PORT = "system.internal.server.mqtt.tcp.port";
    public static final int CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_TCP_PORT = 1883;

    public static final String CONFIGURATION_SERVER_MQTT_SSL_PORT = "system.internal.server.mqtt.tcp.port";
    public static final int CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_SSL_PORT = 8883;

    public static final String CONFIGURATION_SERVER_MQTT_SSL_IS_ENABLED = "system.internal.server.mqtt.ssl.is.enabled";
    public static final boolean CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_SSL_IS_ENABLED = true;

    public static final String CONFIGURATION_SERVER_MQTT_CONNECTION_TIMEOUT = "system.internal.server.mqtt.connection.timeout";
    public static final int CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_CONNECTION_TIMEOUT = 10;


    public MqttServerImpl(Server<MqttMessage> internalServer) {
        super(internalServer);
    }

    /**
     * @param configuration Object carrying all configurable properties from file.
     * @link configure method supplies the configuration object carrying all the
     * properties parsed from the external properties file.
     */

    public void configure(Configuration configuration) {
        log.info(" configure : setting up our configurations.");

        int tcpPort = configuration.getInt(CONFIGURATION_SERVER_MQTT_TCP_PORT, CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_TCP_PORT);
        setTcpPort(tcpPort);

        int sslPort = configuration.getInt(CONFIGURATION_SERVER_MQTT_SSL_PORT, CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_SSL_PORT);
        setSslPort(sslPort);

        boolean sslEnabled = configuration.getBoolean(CONFIGURATION_SERVER_MQTT_SSL_IS_ENABLED, CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_SSL_IS_ENABLED);
        setSslEnabled(sslEnabled);

        if (isSslEnabled()) {

            setSslHandler(new SSLHandler(configuration));

        }

        int connectionTimeout = configuration.getInt(CONFIGURATION_SERVER_MQTT_CONNECTION_TIMEOUT, CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_CONNECTION_TIMEOUT);
        setConnectionTimeout(connectionTimeout);

    }


    @Override
    protected ServerInitializer<MqttMessage> getServerInitializer(ServerImpl<MqttMessage> serverImpl, int connectionTimeout) {
        return new MqttServerInitializer(serverImpl, connectionTimeout);
    }

    @Override
    protected ServerInitializer<MqttMessage> getServerInitializer(ServerImpl<MqttMessage> serverImpl, int connectionTimeout, SSLHandler sslHandler) {
        return new MqttServerInitializer(serverImpl, connectionTimeout, sslHandler);
    }

    @Override
    public void postProcess(IOTMessage ioTMessage) {

        switch (ioTMessage.getMessageType()) {
            case ConnectAcknowledgeMessage.MESSAGE_TYPE:

                ConnectAcknowledgeMessage conMessage = (ConnectAcknowledgeMessage) ioTMessage;


                /**
                 * Use the connection acknowledgement message to store session id for persistance.
                 */

                Channel channel = getChannel((ChannelId) ioTMessage.getConnectionId());
                if (null != channel) {

                    if (MqttConnectReturnCode.CONNECTION_ACCEPTED.equals(conMessage.getReturnCode())) {

                        channel.attr(ServerImpl.REQUEST_SESSION_ID).set(ioTMessage.getSessionId());
                    } else {
                        closeClient((ChannelId) ioTMessage.getConnectionId());
                    }
                }

                break;
            default:
                super.postProcess(ioTMessage);
        }


    }


}
