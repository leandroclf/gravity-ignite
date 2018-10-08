package br.com.trustsystems.gravity.core.handlers;


import br.com.trustsystems.gravity.core.security.AuthorityRole;
import br.com.trustsystems.gravity.core.worker.state.messages.AcknowledgeMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import rx.Observable;

public class PublishAcknowledgeHandler extends RequestHandler<AcknowledgeMessage> {

    public PublishAcknowledgeHandler(AcknowledgeMessage message) {
        super(message);
    }

    @Override
    public void handle() throws RetriableException, UnRetriableException {

        //Check for connect permissions
        Observable<Client> permissionObservable = checkPermission(getMessage().getSessionId(),
                getMessage().getAuthKey(), AuthorityRole.CONNECT);

        permissionObservable.subscribe(

                (client) -> {

                    //Handle acknowledging of message.

                    Observable<PublishMessage> messageObservable = getDatastore().getMessage(
                client.getPartition(), client.getClientId(),
                getMessage().getMessageId(), false);

        messageObservable.subscribe(getDatastore()::removeMessage);

                }, this::disconnectDueToError);

    }
}
