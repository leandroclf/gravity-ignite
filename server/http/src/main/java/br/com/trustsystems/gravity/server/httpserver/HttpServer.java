package br.com.trustsystems.gravity.server.httpserver;

import br.com.trustsystems.gravity.core.modules.Server;
import br.com.trustsystems.gravity.core.modules.Worker;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.Protocol;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.server.ServerInterface;
import br.com.trustsystems.gravity.server.httpserver.netty.HttpServerImpl;
import br.com.trustsystems.gravity.server.httpserver.transform.HttpIOTTransformerImpl;
import br.com.trustsystems.gravity.server.httpserver.transform.IOTHttpTransformerImpl;
import br.com.trustsystems.gravity.server.transform.IOTMqttTransformer;
import br.com.trustsystems.gravity.server.transform.MqttIOTTransformer;
import io.netty.handler.codec.http.FullHttpMessage;
import org.apache.commons.configuration.Configuration;

public class HttpServer extends Server<FullHttpMessage> {

    private ServerInterface<FullHttpMessage> serverImpl;
    private IOTMqttTransformer<FullHttpMessage> iotHttpTransformer;
    private MqttIOTTransformer<FullHttpMessage> httpIOTTransformer;

    /**
     * <code>configure</code> allows the base system to configure itself by getting
     * all the settings it requires and storing them internally. The plugin is only expected to
     * pick the settings it has registered on the configuration file for its particular use.
     *
     * @param configuration
     * @throws UnRetriableException
     */
    @Override
    public void configure(Configuration configuration) throws UnRetriableException {

        log.info(" configure : setting up our configurations.");

        serverImpl = new HttpServerImpl(this);
        serverImpl.configure(configuration);

        iotHttpTransformer = new IOTHttpTransformerImpl();
        httpIOTTransformer = new HttpIOTTransformerImpl();
    }

    /**
     * <code>initiate</code> starts the operations of this system handler.
     * All excecution code for the plugins is expected to begin at this point.
     *
     * @throws UnRetriableException
     */
    @Override
    public void initiate() throws UnRetriableException {

        log.info(" configure : initiating the netty server.");
        serverImpl.initiate();
    }

    /**
     * <code>terminate</code> halts excecution of this plugin.
     * This provides a clean way to exit /stop operations of this particular plugin.
     */
    @Override
    public void terminate() {

        log.info(" terminate : stopping any processing. ");
        serverImpl.terminate();
    }

    /**
     * Provides the Observer with a new item to observe.
     * <p>
     * The {@link Worker} may call this method 0 or more times.
     * <p>
     * The {@code Observable} will not call this method again after it calls either {@link #onCompleted} or
     * {@link #onError}.
     *
     * @param ioTMessage the item emitted by the Observable
     */
    @Override
    public void onNext(IOTMessage ioTMessage) {

        if (null == ioTMessage || !Protocol.HTTP.equals(ioTMessage.getProtocol())) {
            return;
        }

        log.debug(" HttpServer onNext : message outbound {}", ioTMessage);


        FullHttpMessage mqttMessage = toServerMessage(ioTMessage);

        if (null == mqttMessage) {
            log.debug(" HttpServer onNext : ignoring outbound message {}", ioTMessage);
        } else {
            serverImpl.pushToClient(ioTMessage.getConnectionId(), mqttMessage);
        }
        serverImpl.postProcess(ioTMessage);
    }


    @Override
    protected IOTMessage toIOTMessage(FullHttpMessage serverMessage) {
        return httpIOTTransformer.toIOTMessage(serverMessage);
    }


    @Override
    protected FullHttpMessage toServerMessage(IOTMessage internalMessage) {
        return iotHttpTransformer.toServerMessage(internalMessage);
    }

    /**
     * Declaration by the server implementation if its connections are persistant
     * Or not.
     * Persistent connections are expected to store some control data within the server
     * to ensure successive requests are identifiable.
     *
     * @return
     */
    @Override
    public boolean isPersistentConnection() {
        return false;
    }

    @Override
    public Protocol getProtocal() {
        return Protocol.HTTP;
    }
}
