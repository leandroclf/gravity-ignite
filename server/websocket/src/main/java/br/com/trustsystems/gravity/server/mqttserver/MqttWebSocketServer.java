package br.com.trustsystems.gravity.server.mqttserver;

import br.com.trustsystems.gravity.core.modules.Server;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.Protocol;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.server.ServerInterface;
import br.com.trustsystems.gravity.server.mqttserver.netty.MqttSocketServerImpl;
import br.com.trustsystems.gravity.server.mqttserver.transform.IOTMqttTransformerImpl;
import br.com.trustsystems.gravity.server.mqttserver.transform.MqttIOTTransformerImpl;
import br.com.trustsystems.gravity.server.transform.IOTMqttTransformer;
import br.com.trustsystems.gravity.server.transform.MqttIOTTransformer;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.apache.commons.configuration.Configuration;

public class MqttWebSocketServer extends Server<MqttMessage> {

    private ServerInterface<MqttMessage> serverImpl;
    private IOTMqttTransformer<MqttMessage> iotMqttTransformer;
    private MqttIOTTransformer<MqttMessage> mqttIOTTransformer;

    @Override
    public void configure(Configuration configuration) throws UnRetriableException {

        log.info(" configure : setting up our configurations.");

        serverImpl = new MqttSocketServerImpl(this);
        serverImpl.configure(configuration);

        iotMqttTransformer = new IOTMqttTransformerImpl();
        mqttIOTTransformer = new MqttIOTTransformerImpl();
    }

    @Override
    public void initiate() throws UnRetriableException {

        log.info(" configure : initiating the netty server.");
        serverImpl.initiate();
    }

    @Override
    public void terminate() {

        log.info(" terminate : stopping any processing. ");
        serverImpl.terminate();
    }

    @Override
    public void onNext(IOTMessage ioTMessage) {

        if (null == ioTMessage || !Protocol.MQTT.equals(ioTMessage.getProtocol())) {
            return;
        }

        log.debug(" MqttWebSocketServer onNext : message outbound {}", ioTMessage);


        MqttMessage mqttMessage = toServerMessage(ioTMessage);

        if (null == mqttMessage) {
            log.debug(" MqttWebSocketServer onNext : ignoring outbound message {}", ioTMessage);
        } else {
            serverImpl.pushToClient(ioTMessage.getConnectionId(), mqttMessage);
        }
        serverImpl.postProcess(ioTMessage);
    }

    @Override
    protected IOTMessage toIOTMessage(MqttMessage serverMessage) {
        return mqttIOTTransformer.toIOTMessage(serverMessage);
    }

    @Override
    protected MqttMessage toServerMessage(IOTMessage internalMessage) {
        return iotMqttTransformer.toServerMessage(internalMessage);
    }

    @Override
    public boolean isPersistentConnection() {
        return true;
    }

    @Override
    public Protocol getProtocal() {
        return Protocol.MQTT;
    }
}
