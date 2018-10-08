package br.com.trustsystems.gravity.core.handlers;


import br.com.trustsystems.gravity.core.security.AuthorityRole;
import br.com.trustsystems.gravity.core.worker.state.messages.UnSubscribeAcknowledgeMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.UnSubscribeMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.core.worker.state.models.Subscription;
import rx.Observable;

public class UnSubscribeHandler extends RequestHandler<UnSubscribeMessage> {

    public UnSubscribeHandler(UnSubscribeMessage message) {
        super(message);
    }

    @Override
    public void handle() {


        /**
         * If a Server receives an UNSUBSCRIBE packet that contains multiple Topic
         * Filters it MUST handle that packet as if it had received a sequence of
         * multiple UNSUBSCRIBE packets, except that it sends just one UNSUBACK response
         */


        /**
         * Before unsubscribing we should get the current session and validate it.
         */

        Observable<Client> permittedObservable = checkPermission(getMessage().getSessionId(),
                getMessage().getAuthKey(), AuthorityRole.SUBSCRIBE,
                getMessage().getTopicFilterList());

        permittedObservable.subscribe(client -> {

            Observable<Subscription> subscriptionObservable = getDatastore().getSubscriptions(client);

            subscriptionObservable.subscribe(
                    subscription -> {

                        getMessenger().unSubscribe(subscription);
                        // and delete it from our db
                        getDatastore().removeSubscription(subscription);
                    },
                    throwable ->
                            log.error(" handle : problems unsubscribing ", throwable)


            );


            UnSubscribeAcknowledgeMessage unSubscribeAcknowledgeMessage = UnSubscribeAcknowledgeMessage.from(getMessage().getMessageId());
            unSubscribeAcknowledgeMessage.copyBase(getMessage());
            pushToServer(unSubscribeAcknowledgeMessage);


        }, this::disconnectDueToError);

    }


}
