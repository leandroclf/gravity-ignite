package br.com.trustsystems.gravity.core.handlers;


import br.com.trustsystems.gravity.core.security.AuthorityRole;
import br.com.trustsystems.gravity.core.worker.state.messages.CompleteMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import rx.Observable;

public class PublishCompleteHandler extends RequestHandler<CompleteMessage> {


    public PublishCompleteHandler(CompleteMessage message) {
        super( message);
    }

    @Override
    public void handle() throws RetriableException, UnRetriableException {

        //Check for connect permissions
        Observable<Client> permissionObservable = checkPermission(getMessage().getSessionId(),
                getMessage().getAuthKey(), AuthorityRole.CONNECT);

        permissionObservable.subscribe(

                (client) -> {

                    //Now deal with removing the message from the database.
                    Observable<PublishMessage> messageObservable = getDatastore().getMessage(
                            client.getPartition(), client.getClientId(),
                            getMessage().getMessageId(), false);

                    messageObservable.subscribe(getDatastore()::removeMessage);

                }, this::disconnectDueToError);

    }

}
