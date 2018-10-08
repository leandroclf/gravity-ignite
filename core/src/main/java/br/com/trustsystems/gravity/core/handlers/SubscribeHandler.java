package br.com.trustsystems.gravity.core.handlers;


import br.com.trustsystems.gravity.core.security.AuthorityRole;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.RetainedMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.SubscribeAcknowledgeMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.SubscribeMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.core.worker.state.models.SubscriptionFilter;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubscribeHandler extends RequestHandler<SubscribeMessage> {

    public SubscribeHandler(SubscribeMessage message) {
        super(message);
    }

    /**
     * The SUBSCRIBE Packet is sent from the Client to the Server to create one or more
     * Subscriptions. Each SubscriptionFilter registers a Client’s interest in one or more Topics.
     * The Server sends PUBLISH Packets to the Client in order to forward Application Messages
     * that were published to Topics that match these Subscriptions.
     * The SUBSCRIBE Packet also specifies (for each SubscriptionFilter) the maximum QoS with which
     * the Server can send Application Messages to the Client
     *
     * @throws RetriableException
     * @throws UnRetriableException
     */
    @Override
    public void handle() throws RetriableException, UnRetriableException {

        log.debug(" handle : begining to handle a subscription {}.", getMessage());

           /**
             * First we obtain the client responsible for this connection.
             */

            List<Integer> grantedQos = new ArrayList<>();


                /**
                 * Before subscribing we should get the current session and validate it.
                 */

                    List<String> topics = new ArrayList<>();
                    getMessage().getTopicFilterList().forEach(topic -> topics.add(topic.getKey()));

                        Observable<Client> permissionObservable = checkPermission(getMessage().getSessionId(),
                                getMessage().getAuthKey(), AuthorityRole.SUBSCRIBE, topics);

                        permissionObservable.subscribe(
                                (client)->{

                                    //We have all the security to proceed.
                            Observable<Map.Entry<String, Integer>> subscribeObservable = getMessenger().subscribe(client, getMessage().getTopicFilterList());

                            subscribeObservable.subscribe(
                                    (entry) -> grantedQos.add(entry.getValue()),
                                    this::disconnectDueToError,
                                    () -> {

                                        /**
                                         * Save subscription payload
                                         */
                                        if(getMessage().getProtocol().isNotPersistent()){
                                            client.setProtocalData(getMessage().getReceptionUrl());
                                            getDatastore().saveClient(client);
                                        }

                                        SubscribeAcknowledgeMessage subAckMessage = SubscribeAcknowledgeMessage.from(
                                                getMessage().getMessageId(), grantedQos);
                                        subAckMessage.copyBase(getMessage());
                                        pushToServer(subAckMessage);


                                        /**
                                         * Queue retained messages to our subscriber.
                                         */


                                        int count = 0;
                                        for(Map.Entry<String, Integer> entry : getMessage().getTopicFilterList()){
                                            if(grantedQos.get(count++) != 0x80  ){
                                                Observable<SubscriptionFilter> subscriptionFilterObservable = getDatastore().getSubscriptionFilter(client.getPartition(), entry.getKey());
                                                subscriptionFilterObservable.subscribe(
                                                        subscriptionFilter -> {

                                                            Observable<RetainedMessage> retainedMessageObservable = getDatastore().getRetainedMessage(client.getPartition(), subscriptionFilter.getId());
                                                            retainedMessageObservable.subscribe(retainedMessage -> {

                                                                PublishMessage publishMessage = retainedMessage.toPublishMessage();
                                                                publishMessage.setPartition(client.getPartition());
                                                                publishMessage.setClientId(client.getClientId());
                                                                publishMessage.copyBase(getMessage());

                                                                if (publishMessage.getQos() > 0) {

                                                                    publishMessage.setReleased(false);

                                                                    //Save the message as we proceed.
                                                                    getDatastore().saveMessage(publishMessage);
                                                                }

                                                                PublishOutHandler publishOutHandler = new PublishOutHandler(publishMessage,client.getProtocalData());
                                                                publishOutHandler.setWorker(getWorker());
                                                                try {
                                                                    publishOutHandler.handle();
                                                                } catch (RetriableException | UnRetriableException e) {
                                                                    log.error(" handle : problems publishing ", e);
                                                                 }


                                                            }, throwable -> {});
                                                        }
                                                );

                                            }
                                        }

                                    });
                        }, this::disconnectDueToError);


    }
}
