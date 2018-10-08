package br.com.trustsystems.gravity.core.handlers;


import br.com.trustsystems.gravity.core.modules.Datastore;
import br.com.trustsystems.gravity.core.modules.Worker;
import br.com.trustsystems.gravity.core.security.AuthorityRole;
import br.com.trustsystems.gravity.core.worker.state.Messenger;
import br.com.trustsystems.gravity.core.worker.state.messages.DisconnectMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.security.IOTSecurityManager;
import br.com.trustsystems.gravity.security.realm.auth.IdConstruct;
import br.com.trustsystems.gravity.security.realm.auth.permission.IOTPermission;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public abstract class RequestHandler<T extends IOTMessage> {

    protected static final String SESSION_AUTH_KEY = "auth_key";
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final T message;

    private Worker worker;

    public RequestHandler(T message) {
        this.message = message;
    }

    public T getMessage() {
        return message;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Datastore getDatastore() {
        return getWorker().getDatastore();
    }

    public Messenger getMessenger() {
        return getWorker().getMessenger();
    }

    private IOTPermission getPermission(String partition, String username, String clientId, AuthorityRole role, String topic) {
        String permissionString = new StringJoiner("")
                .add(role.name())
                .add(":").add(topic).toString();

        return new IOTPermission(partition, username, clientId, permissionString);
    }

    public Observable<Client> checkPermission(Serializable sessionId, String authKey, AuthorityRole role, String... topicList) {
        return checkPermission(sessionId, authKey, role, Arrays.asList(topicList));

    }

    public Observable<Client> checkPermission(Serializable sessionId, String authKey, AuthorityRole role, List<String> topicList) {

        return Observable.create(observable -> {

            Subject subject = new Subject.Builder().sessionId(sessionId).buildSubject();

            final Session session = subject.getSession(false);

            if (session != null && subject.isAuthenticated()) {


                try {


                    PrincipalCollection principales = (PrincipalCollection) session.getAttribute(IOTSecurityManager.SESSION_PRINCIPLES_KEY);
                    IdConstruct construct = (IdConstruct) principales.getPrimaryPrincipal();

                    String partition = construct.getPartition();
                    String username = construct.getUsername();
                    String session_client_id = construct.getClientId();


                    String session_auth_key = (String) session.getAttribute(SESSION_AUTH_KEY);


                    /**
                     * Make sure for non persistent connections the authKey matches
                     * the stored authKey. Otherwise fail the request.
                     */
                    if (!StringUtils.isEmpty(session_auth_key)) {
                        if (!session_auth_key.equals(authKey))
                            throw new UnauthenticatedException("Client fails auth key assertion.");

                    }

                    if (AuthorityRole.CONNECT.equals(role)) {
                        //No need to check for this permission.
                    } else {

                        List<Permission> permissions = topicList
                                .stream()
                                .map(topic ->
                                        getPermission(
                                                username, partition,
                                                session_client_id,
                                                role, topic))
                                .collect(Collectors.toList());


                        subject.checkPermissions(permissions);
                    }

                    //Update session last accessed time.
                    session.touch();

                    Observable<Client> clientObservable = getDatastore().getClient(partition, session_client_id);

                    clientObservable.subscribe(observable::onNext, observable::onError, observable::onCompleted);


                } catch (AuthorizationException e) {
                    //Notify failure to authorize user.
                    observable.onError(e);
                }

            } else {
                observable.onError(new AuthenticationException("Client must be authenticated {Try connecting first} found : " + session));
            }

        });

    }


    public void disconnectDueToError(Throwable e) {
        log.warn(" disconnectDueToError : System experienced the error ", e);

        //Notify the server to remove this client from further sending in requests.
        DisconnectMessage disconnectMessage = DisconnectMessage.from(true);
        disconnectMessage.copyBase(getMessage());

        DisconnectHandler handler = new DisconnectHandler(disconnectMessage);
        handler.setWorker(getWorker());

        try {
            handler.handle();
        } catch (RetriableException | UnRetriableException ex) {
            log.error(" disconnectDueToError : issues disconnecting.", ex);
        }


    }

    /**
     * Wrapper method to assist in pushing/routing requests to the client.
     * It provides convenience to access the active worker and push
     * out the available messages to the connected client.
     *
     * @param iotMessage
     */
    public void pushToServer(IOTMessage iotMessage) {


        getWorker().pushToServer(iotMessage);

    }

    public abstract void handle() throws RetriableException, UnRetriableException;
}
