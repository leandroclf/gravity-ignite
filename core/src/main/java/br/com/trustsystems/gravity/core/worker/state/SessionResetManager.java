package br.com.trustsystems.gravity.core.worker.state;

import br.com.trustsystems.gravity.core.handlers.PublishOutHandler;
import br.com.trustsystems.gravity.core.modules.Datastore;
import br.com.trustsystems.gravity.core.modules.Worker;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishReceivedMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.ReleaseMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class SessionResetManager {

    private static final Logger log = LoggerFactory.getLogger(SessionResetManager.class);

    private Worker worker;

    private Datastore datastore;

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public void process(Client client) {

        log.debug(" process : Resetting a session for client {} ", client);


        Observable<PublishMessage> publishMessageObservable = getDatastore().getMessages(client);

        publishMessageObservable.subscribe(publishMessage -> {

            publishMessage = client.copyTransmissionData(publishMessage);
            //Update current session id for message.

            if (publishMessage.isInBound()) {

                //We need to generate a PUBREC message to acknowledge message received.
                if (publishMessage.getQos() == MqttQoS.EXACTLY_ONCE.value()) {


                    PublishReceivedMessage publishReceivedMessage = PublishReceivedMessage.from(publishMessage.getMessageId());
                    publishReceivedMessage.copyBase(publishMessage);
                    getWorker().pushToServer(publishReceivedMessage);


                }

            } else {

                if (publishMessage.getQos() == MqttQoS.EXACTLY_ONCE.value() && publishMessage.isReleased()) {

                    //We need to generate a PUBREL message to allow transmission of qos 2 message.
                    ReleaseMessage releaseMessage = ReleaseMessage.from(publishMessage.getMessageId(), true);
                    releaseMessage.copyBase(publishMessage);
                    getWorker().pushToServer(releaseMessage);


                } else {

                    //This message should be released to the client
                    PublishOutHandler handler = new PublishOutHandler(publishMessage, client.getProtocalData());
                    handler.setWorker(getWorker());

                    try {
                        handler.handle();
                    } catch (RetriableException | UnRetriableException e) {
                        log.error(" process : problems releasing stored messages", e);
                    }

                }
            }

        });


    }


}
