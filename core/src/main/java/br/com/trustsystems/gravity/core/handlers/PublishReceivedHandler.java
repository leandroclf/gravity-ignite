package br.com.trustsystems.gravity.core.handlers;

import br.com.trustsystems.gravity.core.security.AuthorityRole;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishReceivedMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.ReleaseMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import rx.Observable;

public class PublishReceivedHandler extends RequestHandler<PublishReceivedMessage> {


    public PublishReceivedHandler(PublishReceivedMessage message) {
        super(message);

    }

    @Override
    public void handle() throws RetriableException, UnRetriableException {


//Check for connect permissions
        Observable<Client> permissionObservable = checkPermission(getMessage().getSessionId(),
                getMessage().getAuthKey(), AuthorityRole.CONNECT);

        permissionObservable.subscribe(

                (client) -> {

                    Observable<PublishMessage> messageObservable = getDatastore().getMessage(
                            client.getPartition(), client.getClientId(),
                            getMessage().getMessageId(), false);

                    messageObservable.subscribe(publishMessage -> {

                        log.debug(" handle : Obtained the message {} to be released.", publishMessage);

                        publishMessage.setReleased(true);


                        Observable<Long> messageIdObservable = getDatastore().saveMessage(publishMessage);
                        messageIdObservable.subscribe(messageId -> {

                            //Generate a PUBREL message.

                            ReleaseMessage releaseMessage = ReleaseMessage.from(messageId, false);
                            releaseMessage.copyBase(getMessage());
                            pushToServer(releaseMessage);

                        });

                    });

                }, this::disconnectDueToError);
    }


}
