package br.com.trustsystems.gravity.core.handlers;


import br.com.trustsystems.gravity.core.security.AuthorityRole;
import br.com.trustsystems.gravity.core.worker.state.messages.Ping;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import rx.Observable;

public class PingRequestHandler extends RequestHandler<Ping> {

   public PingRequestHandler(Ping message) {
    super(message);
   }

    @Override
    public void handle() throws RetriableException, UnRetriableException {

        Observable<Client> permissionObservable = checkPermission(getMessage().getSessionId(),
                getMessage().getAuthKey(), AuthorityRole.CONNECT);

        permissionObservable.subscribe(

                (client) -> {

                    try {

                        //TODO: deal with ping issues.
                        pushToServer(getMessage());

                        getWorker().getSessionResetManager().process(client);


                    } catch (Exception e) {
                        log.error(" handle : ping handler experienced issues", e);

                    }


                }, this::disconnectDueToError);

    }
}
