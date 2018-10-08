package br.com.trustsystems.gravity.core.handlers;


import br.com.trustsystems.gravity.core.security.AuthorityRole;
import br.com.trustsystems.gravity.core.worker.state.messages.DisconnectMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.shiro.subject.Subject;
import rx.Observable;

import java.io.Serializable;

public class DisconnectHandler extends RequestHandler<DisconnectMessage> {


    public DisconnectHandler(DisconnectMessage message) {
        super(message);
    }

    @Override
    public void handle() throws RetriableException, UnRetriableException {


        /**
         * Before disconnecting we should get the current session and close it
         * then close the network connection.
         */

        Observable<Client> permissionObservable = checkPermission(getMessage().getSessionId(),
                getMessage().getAuthKey(), AuthorityRole.CONNECT);

        permissionObservable.subscribe(
                (client) -> {



                    if (getMessage().isDirtyDisconnect()) {
                          getWorker().publishWill(client);
                    }

                    logOutSession(client.getSessionId());

                }, (throwable -> {
                    log.warn(" handle : attempting to disconnect errorfull person.", throwable);
                }));


    }


    private void logOutSession(Serializable sessionId) {
       try {

           Subject subject = new Subject.Builder().sessionId(sessionId).buildSubject();
           subject.logout();

       }catch (Exception e){
           log.error(" logOutSession : problems during disconnection ", e);
       }

    }



}
