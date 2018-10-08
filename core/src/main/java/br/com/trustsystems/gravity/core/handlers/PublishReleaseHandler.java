package br.com.trustsystems.gravity.core.handlers;

import br.com.trustsystems.gravity.core.security.AuthorityRole;
import br.com.trustsystems.gravity.core.worker.state.messages.CompleteMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.ReleaseMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import rx.Observable;

public class PublishReleaseHandler extends RequestHandler<ReleaseMessage> {

    public PublishReleaseHandler(ReleaseMessage message) {
        super(message);
    }

    @Override
    public void handle() {

        //Check for connect permissions
        Observable<Client> permissionObservable = checkPermission(getMessage().getSessionId(),
                getMessage().getAuthKey(), AuthorityRole.CONNECT);
        permissionObservable.subscribe(

                (client) -> {


                    /**
                     * MUST respond to a PUBREL packet by sending a PUBCOMP packet containing the same Packet Identifier as the PUBREL.
                     * After it has sent a PUBCOMP, the receiver MUST treat any subsequent PUBLISH packet that contains that Packet Identifier as being a new publication.
                     */


                    Observable<PublishMessage> messageObservable = getDatastore().getMessage(
                            client.getPartition(), client.getClientId(), getMessage().getMessageId(), true);

                    messageObservable.subscribe(publishMessage -> {

                        publishMessage.setReleased(true);
                        Observable<Long> messageIdObservable = getDatastore().saveMessage(publishMessage);

                        messageIdObservable.subscribe(messageId -> {
                            try {

                                client.internalPublishMessage(getMessenger(), publishMessage);

                                //Initiate a publish complete.
                                CompleteMessage destroyMessage = CompleteMessage.from(publishMessage.getMessageId());
                                destroyMessage.copyBase(publishMessage);
                                pushToServer(destroyMessage);


                                //Destroy message.
                                getDatastore().removeMessage(publishMessage);

                            } catch (RetriableException e) {
                                log.error(" releaseInboundMessage : encountered a problem while publishing.", e);
                            }
                        });
                    });
                }, this::disconnectDueToError);
    }


}
