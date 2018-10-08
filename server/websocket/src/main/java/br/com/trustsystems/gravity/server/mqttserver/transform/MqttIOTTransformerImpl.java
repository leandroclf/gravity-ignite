package br.com.trustsystems.gravity.server.mqttserver.transform;

import br.com.trustsystems.gravity.core.worker.state.messages.*;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.server.transform.MqttIOTTransformer;
import io.netty.handler.codec.mqtt.*;

import java.nio.ByteBuffer;
import java.util.AbstractMap;

public class MqttIOTTransformerImpl implements MqttIOTTransformer<MqttMessage> {

    @Override
    public IOTMessage toIOTMessage(MqttMessage serverMessage) {


        MqttFixedHeader fxH = serverMessage.fixedHeader();


        if (null == fxH) {
            return null;
        }

        switch (fxH.messageType()) {


            case PUBLISH:

                MqttPublishMessage publishMessage = (MqttPublishMessage) serverMessage;

                MqttPublishVariableHeader pubVH = publishMessage.variableHeader();

                ByteBuffer byteBuffer = publishMessage.payload().nioBuffer();

                return PublishMessage.from(pubVH.messageId(), fxH.isDup(), fxH.qosLevel().value(),
                        fxH.isRetain(), pubVH.topicName(), byteBuffer, true);


            case PUBACK:

                MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) serverMessage;

                MqttMessageIdVariableHeader msgIdVH = pubAckMessage.variableHeader();
                return AcknowledgeMessage.from(msgIdVH.messageId());


            case PUBREC:

                msgIdVH = (MqttMessageIdVariableHeader) serverMessage.variableHeader();
                return PublishReceivedMessage.from(msgIdVH.messageId());

            case PUBREL:

                msgIdVH = (MqttMessageIdVariableHeader) serverMessage.variableHeader();
                return ReleaseMessage.from(msgIdVH.messageId(), fxH.isDup());

            case PUBCOMP:

                msgIdVH = (MqttMessageIdVariableHeader) serverMessage.variableHeader();

                return CompleteMessage.from(msgIdVH.messageId());
            case PINGREQ:
            case PINGRESP:
                return Ping.from(fxH.isDup(), fxH.qosLevel().value(), fxH.isRetain());

            case CONNECT:

                MqttConnectMessage mqttConnectMessage = (MqttConnectMessage) serverMessage;
                MqttConnectVariableHeader conVH = mqttConnectMessage.variableHeader();
                MqttConnectPayload conPayload = mqttConnectMessage.payload();

                boolean isAnnonymousConnect = (!conVH.hasPassword() && !conVH.hasUserName());

                ConnectMessage connectionMessage = ConnectMessage.from(fxH.isDup(), fxH.qosLevel().value(), fxH.isRetain(),
                        conVH.name(), conVH.version(), conVH.isCleanSession(), isAnnonymousConnect, conPayload.clientIdentifier(),
                        conPayload.userName(), conPayload.password(), conVH.keepAliveTimeSeconds(), "");

                connectionMessage.setHasWill(conVH.isWillFlag());
                connectionMessage.setRetainWill(conVH.isWillRetain());
                connectionMessage.setWillQos(conVH.willQos());
                connectionMessage.setWillTopic(conPayload.willTopic());
                connectionMessage.setWillMessage(conPayload.willMessage());
                return connectionMessage;

            case CONNACK:

                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) serverMessage;
                MqttConnAckVariableHeader connAckVH = connAckMessage.variableHeader();


                return ConnectAcknowledgeMessage.from(fxH.isDup(), fxH.qosLevel().value(), fxH.isRetain(), 20, connAckVH.connectReturnCode());

            case SUBSCRIBE:

                MqttSubscribeMessage subMsg = (MqttSubscribeMessage) serverMessage;
                msgIdVH = subMsg.variableHeader();
                MqttSubscribePayload subPayload = subMsg.payload();

                SubscribeMessage subscribeMessage = SubscribeMessage.from(msgIdVH.messageId(), fxH.isDup(), fxH.qosLevel().value(), fxH.isRetain());

                subPayload.topicSubscriptions().forEach(tSub -> {
                    subscribeMessage.getTopicFilterList().add(
                            new AbstractMap.SimpleEntry<>(tSub.topicName(), tSub.qualityOfService().value()));
                });


                return subscribeMessage;

            case UNSUBSCRIBE:

                MqttUnsubscribeMessage unSubMsg = (MqttUnsubscribeMessage) serverMessage;

                msgIdVH = unSubMsg.variableHeader();
                MqttUnsubscribePayload unsubscribePayload = unSubMsg.payload();

                return UnSubscribeMessage.from(msgIdVH.messageId(), fxH.isDup(), fxH.qosLevel().value(), fxH.isRetain(), unsubscribePayload.topics());

            case DISCONNECT:
                return DisconnectMessage.from(false);


            default:
                return null;
        }
    }
}
