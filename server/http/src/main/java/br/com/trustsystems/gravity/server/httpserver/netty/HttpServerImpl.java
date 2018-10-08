package br.com.trustsystems.gravity.server.httpserver.netty;

import br.com.trustsystems.gravity.core.modules.Server;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.server.netty.SSLHandler;
import br.com.trustsystems.gravity.server.netty.ServerImpl;
import br.com.trustsystems.gravity.server.netty.ServerInitializer;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.FullHttpMessage;
import org.apache.commons.configuration.Configuration;

public class HttpServerImpl extends ServerImpl<FullHttpMessage> {


    public static final String CONFIGURATION_SERVER_HTTP_TCP_PORT = "system.internal.server.http.tcp.port";
    public static final int CONFIGURATION_VALUE_DEFAULT_SERVER_HTTP_TCP_PORT = 7180;

    public static final String CONFIGURATION_SERVER_HTTP_SSL_PORT = "system.internal.server.http.tcp.port";
    public static final int CONFIGURATION_VALUE_DEFAULT_SERVER_HTTP_SSL_PORT = 7183;

    public static final String CONFIGURATION_SERVER_HTTP_SSL_IS_ENABLED = "system.internal.server.http.ssl.is.enabled";
    public static final boolean CONFIGURATION_VALUE_DEFAULT_SERVER_HTTP_SSL_IS_ENABLED = true;

    public static final String CONFIGURATION_SERVER_HTTP_CONNECTION_TIMEOUT = "system.internal.server.http.connection.timeout";
    public static final int CONFIGURATION_VALUE_DEFAULT_SERVER_HTTP_CONNECTION_TIMEOUT = 10;

    public HttpServerImpl(Server<FullHttpMessage> internalServer) {
        super(internalServer);
    }

    /**
     * @param configuration Object carrying all configurable properties from file.
     * @link configure method supplies the configuration object carrying all the
     * properties parsed from the external properties file.
     */

    public void configure(Configuration configuration) {
        log.info(" configure : setting up our configurations.");

        int tcpPort = configuration.getInt(CONFIGURATION_SERVER_HTTP_TCP_PORT, CONFIGURATION_VALUE_DEFAULT_SERVER_HTTP_TCP_PORT);
        setTcpPort(tcpPort);

        int sslPort = configuration.getInt(CONFIGURATION_SERVER_HTTP_SSL_PORT, CONFIGURATION_VALUE_DEFAULT_SERVER_HTTP_SSL_PORT);
        setSslPort(sslPort);

        boolean sslEnabled = configuration.getBoolean(CONFIGURATION_SERVER_HTTP_SSL_IS_ENABLED, CONFIGURATION_VALUE_DEFAULT_SERVER_HTTP_SSL_IS_ENABLED);
        setSslEnabled(sslEnabled);

        if (isSslEnabled()) {

            setSslHandler(new SSLHandler(configuration));

        }

        int connectionTimeout = configuration.getInt(CONFIGURATION_SERVER_HTTP_CONNECTION_TIMEOUT, CONFIGURATION_VALUE_DEFAULT_SERVER_HTTP_CONNECTION_TIMEOUT);
        setConnectionTimeout(connectionTimeout);

    }


    @Override
    protected ServerInitializer<FullHttpMessage> getServerInitializer(ServerImpl<FullHttpMessage> serverImpl, int connectionTimeout) {
        return new HttpServerInitializer(serverImpl, connectionTimeout);
    }

    @Override
    protected ServerInitializer<FullHttpMessage> getServerInitializer(ServerImpl<FullHttpMessage> serverImpl, int connectionTimeout, SSLHandler sslHandler) {
        return new HttpServerInitializer(serverImpl, connectionTimeout, sslHandler);
    }


    @Override
    public void postProcess(IOTMessage ioTMessage) {

        //Always close the connection once there is a response.
        closeClient((ChannelId) ioTMessage.getConnectionId());

    }
}
