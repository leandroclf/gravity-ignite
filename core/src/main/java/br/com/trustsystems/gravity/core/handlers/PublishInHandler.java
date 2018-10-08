package br.com.trustsystems.gravity.core.handlers;

import br.com.trustsystems.gravity.core.security.AuthorityRole;
import br.com.trustsystems.gravity.core.worker.exceptions.ShutdownException;
import br.com.trustsystems.gravity.core.worker.state.Constant;
import br.com.trustsystems.gravity.core.worker.state.messages.AcknowledgeMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishReceivedMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import io.netty.handler.codec.mqtt.MqttQoS;
import rx.Observable;
public class PublishInHandler extends RequestHandler<PublishMessage> {

    public PublishInHandler(PublishMessage message) {
        super(message);
    }


    /**
     * A PUBLISH Control Packet is sent from a Client to a Server or from Server to a Client
     * to transport an Application Message.
     *
     * @throws RetriableException
     * @throws UnRetriableException
     */
    @Override
    public void handle() throws RetriableException, UnRetriableException {

        log.debug(" handle : client attempting to publish a message.");


        /**
         * During an attempt to publish a message.
         *
         * All Topic Names and Topic Filters MUST be at least one character long [MQTT-4.7.3-1]
         *
         * The wildcard characters can be used in Topic Filters,
         * but MUST NOT be used within a Topic Name [MQTT-4.7.1-1].
         *
         * Topic Names and Topic Filters MUST NOT include the null character (Unicode U+0000) [Unicode] [MQTT-4.7.3-2]
         *
         * Topic Names and Topic Filters are UTF-8 encoded strings, they MUST NOT encode to more than 65535 bytes [MQTT-4.7.3-3]. See Section 1.5.3
         *
         */
        String topic = getMessage().getTopic();
        if (null == topic ||
                topic.isEmpty() ||
                topic.contains(Constant.MULTI_LEVEL_WILDCARD) ||
                topic.contains(Constant.SINGLE_LEVEL_WILDCARD) ||
                topic.contains(Constant.SYS_PREFIX)
                ) {
            log.info(" handle : Invalid topic " + getMessage().getTopic());
            throw new ShutdownException(" Invalid topic name");
        }


        /**
         * Before publishing we should get the current session and validate it.
         */
        Observable<Client> permissionObservable = checkPermission(
                getMessage().getSessionId(), getMessage().getAuthKey(),
                AuthorityRole.PUBLISH, topic);

        permissionObservable.subscribe(
                (client) -> {

                    try {

                        getMessage().setPartition(client.getPartition());
                        getMessage().setClientId(client.getClientId());

                        /**
                         * Message processing is based on 4.3 Quality of Service levels and protocol flows
                         */

                        /**
                         *  4.3.1 QoS 0: At most once delivery
                         *  Accepts ownership of the message when it receives the PUBLISH packet.
                         */
                        if (MqttQoS.AT_MOST_ONCE.value() == getMessage().getQos()) {

                            client.internalPublishMessage(getMessenger(), getMessage());
                        }


                        /**
                         * 4.3.2 QoS 1: At least once delivery
                         *
                         * MUST respond with a PUBACK Packet containing the Packet Identifier from the incoming PUBLISH Packet, having accepted ownership of the Application Message
                         * After it has sent a PUBACK Packet the Receiver MUST treat any incoming PUBLISH packet that contains the same Packet Identifier as being a new publication, irrespective of the setting of its DUP flag.
                         */
                        if (MqttQoS.AT_LEAST_ONCE.value() == getMessage().getQos()) {

                            client.internalPublishMessage(getMessenger(), getMessage());


                            //We need to generate a puback message to close this conversation.

                            AcknowledgeMessage acknowledgeMessage = AcknowledgeMessage.from(
                                    getMessage().getMessageId());
                            acknowledgeMessage.copyBase(getMessage());

                            pushToServer(acknowledgeMessage);


                        }


                        /**
                         * 4.3.3 QoS 2: Exactly once delivery
                         *
                         * MUST respond with a PUBREC containing the Packet Identifier from the incoming PUBLISH Packet, having accepted ownership of the Application Message.
                         * Until it has received the corresponding PUBREL packet, the Receiver MUST acknowledge any subsequent PUBLISH packet with the same Packet Identifier by sending a PUBREC.
                         * It MUST NOT cause duplicate messages to be delivered to any onward recipients in this case.
                         *
                         */

                        if (MqttQoS.EXACTLY_ONCE.value() == getMessage().getQos()) {


                            queueQos2Message(getMessage());
                        }

                    } catch (UnRetriableException | RetriableException e) {
                        disconnectDueToError(e);
                    }

                }, this::disconnectDueToError

        );

    }


    private void queueQos2Message(PublishMessage message) throws UnRetriableException {
        if (MqttQoS.EXACTLY_ONCE.value() == message.getQos()) {
            //This message needs to be retained
            // while handshake is completed before being released.

            message.setReleased(false);
            Observable<Long> messageIdObservable = getDatastore().saveMessage(message);

            messageIdObservable.subscribe(messageId -> {

                //We need to push out a PUBREC

                PublishReceivedMessage publishReceivedMessage = PublishReceivedMessage.from(messageId);
                publishReceivedMessage.copyBase(message);
                pushToServer(publishReceivedMessage);

            });


        } else

            throw new UnRetriableException("Only qos 2 messages should be Queued for handshake to occur.");
    }


}
