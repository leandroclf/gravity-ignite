package br.com.trustsystems.gravity.core.handlers;

import br.com.trustsystems.gravity.core.modules.Worker;
import br.com.trustsystems.gravity.core.worker.exceptions.ShutdownException;
import br.com.trustsystems.gravity.core.worker.exceptions.UnknownProtocalException;
import br.com.trustsystems.gravity.core.worker.state.messages.ConnectAcknowledgeMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.ConnectMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.WillMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.Protocol;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.security.IOTSecurityManager;
import br.com.trustsystems.gravity.security.realm.auth.IdConstruct;
import br.com.trustsystems.gravity.security.realm.auth.IdPassToken;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttIdentifierRejectedException;
import io.netty.handler.codec.mqtt.MqttUnacceptableProtocolVersionException;
import io.netty.handler.codec.mqtt.MqttVersion;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import rx.Observable;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionHandler extends RequestHandler<ConnectMessage> {

    private final Pattern usernamePartitionPattern = Pattern.compile("(?<username>.*)-<(?<partition>.*)>");
    private final Pattern pattern = Pattern.compile("[\\w\\-\\s/<>]*");


    public ConnectionHandler(ConnectMessage message) {
        super(message);
    }


    /**
     * * 3.1.4 Response
     * <p>
     * Note that a Server MAY support multiple protocols (including earlier versions of this protocol)
     * on the same TCP port or other network endpoint. If the Server determines that the protocol is MQTT 3.1.1
     * then it validates the connection attempt as follows.
     * <p>
     * 1.     If the Server does not receive a CONNECT Packet within a reasonable amount of time after
     * the Network Connection is established, the Server SHOULD close the connection.
     * <p>
     * 2.     The Server MUST validate that the CONNECT Packet conforms to section 3.1 and close
     * the Network Connection without sending a CONNACK if it does not conform [MQTT-3.1.4-1].
     * <p>
     * 3.     The Server MAY check that the contents of the CONNECT Packet meet any further restrictions
     * and MAY perform authentication and authorization checks. If any of these checks fail,
     * it SHOULD send an appropriate CONNACK response with a non-zero return code as described
     * in section 3.2 and it MUST close the Network Connection.
     * <p>
     * If validation is successful the Server performs the following steps.
     * <p>
     * 1.     If the ClientId represents a Client already connected to the Server then
     * the Server MUST disconnect the existing Client [MQTT-3.1.4-2].
     * 2.     The Server MUST perform the processing of CleanSession that is described
     * in section 3.1.2.4 [MQTT-3.1.4-3].
     * 3.     The Server MUST acknowledge the CONNECT Packet with a CONNACK Packet
     * containing a zero return code [MQTT-3.1.4-4].
     * 4.     Start message delivery and keep alive monitoring.
     * <p>
     * Clients are allowed to send further Control Packets immediately after sending a CONNECT Packet;
     * Clients need not wait for a CONNACK Packet to arrive from the Server. If the Server rejects the CONNECT,
     * it MUST NOT process any data sent by the Client after the CONNECT Packet [MQTT-3.1.4-5].
     * <p>
     * Non normative comment
     * Clients typically wait for a CONNACK Packet,
     * However, if the Client exploits its freedom to send Control Packets before it receives a CONNACK,
     * it might simplify the Client implementation as it does not have to police the connected state.
     * The Client accepts that any data that it sends before it receives a CONNACK packet from the
     * Server will not be processed if the Server rejects the connection.
     *
     * @return
     * @throws UnRetriableException
     */
    @Override
    public void handle() throws UnRetriableException {


        log.debug(" handle : client initiating a new connection.");

        /**
         * 2.     The Server MUST validate that the CONNECT Packet conforms to section 3.1 and close
         *        the Network Connection without sending a CONNACK if it does not conform [MQTT-3.1.4-1].
         *
         *        3.1[ The Server MUST process a second CONNECT Packet sent from a Client as a protocol
         *        violation and disconnect the Client [MQTT-3.1.0-2].  See section 4.8 for information about handling errors.]
         *
         *
         */


        try {

            if (!MqttVersion.MQTT_3_1_1.protocolName().equals(getMessage().getProtocolName())
                    && !MqttVersion.MQTT_3_1.protocolName().equals(getMessage().getProtocolName())
            ) {

                /**
                 * If the protocol name is incorrect the Server MAY disconnect the Client,
                 * or it MAY continue processing the CONNECT packet in accordance with some other specification.
                 * In the latter case, the Server MUST NOT continue to process the CONNECT packet in line with
                 * this specification [MQTT-3.1.2-1].
                 *
                 */

                throw new UnknownProtocalException();

            }


            if (MqttVersion.MQTT_3_1_1.protocolLevel() != getMessage().getProtocalLevel()
                    && MqttVersion.MQTT_3_1.protocolLevel() != getMessage().getProtocalLevel()
            ) {

                /**
                 * The 8 bit unsigned value that represents the revision level of the protocol used by the Client.
                 * The value of the Protocol Level field for the version 3.1.1 of the protocol is 4 (0x04).
                 * The Server MUST respond to the CONNECT Packet with a CONNACK return code 0x01 (unacceptable protocol level)
                 * and then disconnect the Client if the Protocol Level is not supported by the Server [MQTT-3.1.2-2].
                 */

                throw new MqttUnacceptableProtocolVersionException();
            } else {
                log.debug(" handle: the required protocal was selected.");
            }


            //TODO:      The Server MUST validate that the reserved flag in the CONNECT Control Packet is set to zero and disconnect the Client if it is not zero [MQTT-3.1.2-3].


            //We now proceed to openning a session on our core service interface.
            boolean cleanSession = getMessage().isCleanSession();

            /**
             * The Server MUST allow ClientIds which are between 1 and 23 UTF-8 encoded bytes in length,
             * and that contain only the characters
             *           "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" [MQTT-3.1.3-5].
             *
             * The Server MAY allow ClientId’s that contain more than 23 encoded bytes.
             * The Server MAY allow ClientId’s that contain characters not included in the list given above.
             *
             *
             * A Server MAY allow a Client to supply a ClientId that has a length of zero bytes,
             * however if it does so the Server MUST treat this as a special case and assign a unique ClientId to that Client.
             * It MUST then process the CONNECT packet as if the Client had provided that unique ClientId [MQTT-3.1.3-6].
             *
             */
            String clientIdentifier = getMessage().getClientId();


            /**
             * If the Client supplies a zero-byte ClientId, the Client MUST also set CleanSession to 1 [MQTT-3.1.3-7].
             *
             * If the Client supplies a zero-byte ClientId with CleanSession set to 0,
             * the Server MUST respond to the CONNECT Packet with a CONNACK return code 0x02 (Identifier rejected)
             * and then close the Network Connection [MQTT-3.1.3-8].
             */
            if ((null == clientIdentifier || clientIdentifier.isEmpty())) {

                if (!cleanSession) {

                    throw new MqttIdentifierRejectedException();
                }
            } else {

                //Run a regular expression to check for invalid characters in our clientIdentifier.
                if (!pattern.matcher(clientIdentifier).matches()) {

                    throw new MqttIdentifierRejectedException();

                }

            }

            log.debug(" handle: we are ready now to obtain the core session.");

            Observable<Client> newClientObservable = openSubject(getWorker(),
                    getMessage().getCluster(), getMessage().getNodeId(), getMessage().getConnectionId(),
                    clientIdentifier, cleanSession, getMessage().getUserName(), getMessage().getPassword(),
                    getMessage().getKeepAliveTime(), getMessage().getSourceHost(), getMessage().getProtocol()
            );

            newClientObservable.subscribe(

                    (client) -> {

                        getMessage().setSessionId(client.getSessionId());


                        log.debug(" handle: obtained a client : {}. ", client);

                        /**
                         * 3.     The Server MAY check that the contents of the CONNECT Packet meet any further restrictions
                         *        and MAY perform authentication and authorization checks. If any of these checks fail,
                         *        it SHOULD send an appropriate CONNACK response with a non-zero return code as described
                         *      in section 3.2 and it MUST close the Network Connection.
                         *
                         */


                        log.info(" onNext : Successfully initiated a session.");

                        //Respond to server with a connection successfull.
                        ConnectAcknowledgeMessage connectAcknowledgeMessage = ConnectAcknowledgeMessage.from(getMessage().isDup(), getMessage().getQos(), getMessage().isRetain(), getMessage().getKeepAliveTime(), MqttConnectReturnCode.CONNECTION_ACCEPTED);
                        connectAcknowledgeMessage.copyBase(getMessage());

                        pushToServer(connectAcknowledgeMessage);


                        WillMessage will;
                        if (getMessage().isHasWill()) {

                            /**
                             * If the Will Flag is set to 1 this indicates that, if the Connect request is accepted,
                             * a Will Message MUST be stored on the Server and associated with the Network Connection.
                             * The Will Message MUST be published when the Network Connection is subsequently closed unless
                             * the Will Message has been deleted by the Server on receipt of a DISCONNECT Packet [MQTT-3.1.2-8].
                             */


                            will = WillMessage.from(getMessage().isRetainWill(), getMessage().getWillQos(),
                                    getMessage().getWillTopic(), getMessage().getWillMessage());
                            will.setPartition(client.getPartition());
                            will.setClientId(client.getClientId());
                            will.copyBase(getMessage());

                            getDatastore().saveWill(will);

                            log.debug(" handle: message has will : {} ", will);


                        } else {
                            //We need to clear the existing will if any.
                            will = WillMessage.from(false, 0, "", "");
                            will.setPartition(client.getPartition());
                            will.setClientId(client.getClientId());
                            getDatastore().removeWill(will);
                        }


                        //Perform a reset for our session.
                        getWorker()
                                .getSessionResetManager()
                                .process(client);


                    },

                    (e) -> {

                        log.error(" onError : Problems ", e);

                        ConnectAcknowledgeMessage connectAcknowledgeMessage;

                        if (e instanceof AuthenticationException) {

                            connectAcknowledgeMessage = ConnectAcknowledgeMessage.from(getMessage().isDup(), getMessage().getQos(), getMessage().isRetain(), getMessage().getKeepAliveTime(), MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);

                        } else if (e instanceof AuthorizationException) {

                            connectAcknowledgeMessage = ConnectAcknowledgeMessage.from(getMessage().isDup(), getMessage().getQos(), getMessage().isRetain(), getMessage().getKeepAliveTime(), MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED);

                        } else {
                            connectAcknowledgeMessage = ConnectAcknowledgeMessage.from(getMessage().isDup(), getMessage().getQos(), getMessage().isRetain(), getMessage().getKeepAliveTime(), MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE);
                        }

                        connectAcknowledgeMessage.copyBase(getMessage());
                        pushToServer(connectAcknowledgeMessage);

                    }, () -> {

                    });
        } catch (MqttUnacceptableProtocolVersionException | MqttIdentifierRejectedException | AuthenticationException | UnknownProtocalException e) {

            log.debug(" handle : Client connection issues ", e);

            //Respond to server with a connection unsuccessfull.
            ConnectAcknowledgeMessage connectAcknowledgeMessage;

            if (e instanceof MqttIdentifierRejectedException) {
                connectAcknowledgeMessage = ConnectAcknowledgeMessage.from(getMessage().isDup(), getMessage().getQos(), getMessage().isRetain(), getMessage().getKeepAliveTime(), MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);

            } else if (e instanceof MqttUnacceptableProtocolVersionException) {

                connectAcknowledgeMessage = ConnectAcknowledgeMessage.from(getMessage().isDup(), getMessage().getQos(), getMessage().isRetain(), getMessage().getKeepAliveTime(), MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);

            } else if (e instanceof UnknownProtocalException) {

                connectAcknowledgeMessage = ConnectAcknowledgeMessage.from(getMessage().isDup(), getMessage().getQos(), getMessage().isRetain(), getMessage().getKeepAliveTime(), MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);

            } else {
                connectAcknowledgeMessage = ConnectAcknowledgeMessage.from(getMessage().isDup(), getMessage().getQos(), getMessage().isRetain(), getMessage().getKeepAliveTime(), MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE);
            }

            connectAcknowledgeMessage.copyBase(getMessage());
            throw new ShutdownException(connectAcknowledgeMessage);

        } catch (Exception systemError) {

            ConnectAcknowledgeMessage connectAcknowledgeMessage = ConnectAcknowledgeMessage.from(getMessage().isDup(), getMessage().getQos(), getMessage().isRetain(), getMessage().getKeepAliveTime(), MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE);
            connectAcknowledgeMessage.copyBase(getMessage());
            log.error(" handle : System experienced the error ", systemError);
            throw new ShutdownException(connectAcknowledgeMessage);

        }

    }

    private Observable<Client> openSubject(Worker worker, String connectedCluster, UUID connectedNode,
                                           Serializable connectionID, String clientIdentifier, boolean cleanSession,
                                           String userName, String password, int keepAliveTime, String sourceHost,
                                           Protocol protocal) {

        return Observable.create(observable -> {

            try {

                log.debug(" openSubject : create -- initiating subject creation.");

                final String activeClientId;
                if (null == clientIdentifier) {
                    activeClientId = worker.getDatastore().nextClientId();
                } else {
                    activeClientId = clientIdentifier;
                }

                final String partition = processUsernameForPartition(userName);

                Client defaultClient = new Client();
                defaultClient.setConnectedCluster(connectedCluster);
                defaultClient.setConnectedNode(connectedNode);
                defaultClient.setConnectionId(connectionID);
                defaultClient.setClientId(activeClientId);
                defaultClient.setPartition(partition);
                defaultClient.setSessionId(null);
                defaultClient.setActive(false);
                defaultClient.setProtocal(protocal);

                log.debug(" openSubject : create -- Futher into the database.");

                Observable<Client> clientObservable = getDatastore().getClient(partition, activeClientId);
                clientObservable.firstOrDefault(defaultClient).subscribe((client) -> {

                            //We have obtained a client to work with.

                            log.debug(" openSubject : create -- We obtained a client.");

                            try {

                                IdConstruct idConstruct = new IdConstruct(partition, userName, activeClientId);
                                PrincipalCollection principals = new SimplePrincipalCollection(idConstruct, "");

                                Subject.Builder subjectBuilder = new Subject.Builder();
                                subjectBuilder = subjectBuilder.principals(principals);
                                subjectBuilder = subjectBuilder.host(sourceHost);
                                subjectBuilder = subjectBuilder.sessionCreationEnabled(true);
                                subjectBuilder = subjectBuilder.sessionId(client.getSessionId());
                                Subject activeUser = subjectBuilder.buildSubject();

                                if (activeUser.isAuthenticated() && cleanSession) {
                                    //Clean a logged in session.
                                    activeUser.logout();
                                }

                                char[] passwordChars;

                                if (null == password)
                                    passwordChars = null;
                                else
                                    passwordChars = password.toCharArray();


                                IdPassToken token = new IdPassToken(partition, userName, activeClientId, passwordChars);


                                activeUser.login(token);

                                Double keepAliveDisconnectiontime = keepAliveTime * 1.5 * 1000;


                                log.info(" openSubject : Authenticated client <{}> username {} with keep alive of {} seconds", client.getClientId(), userName, keepAliveDisconnectiontime);

                                Session session = activeUser.getSession();
                                session.setTimeout(keepAliveDisconnectiontime.longValue());
                                session.setAttribute(IOTSecurityManager.SESSION_PRINCIPLES_KEY, principals);

                                if (client.getProtocal().isNotPersistent()) {
                                    String sessionAuthKey = generateMAC();
                                    session.setAttribute(SESSION_AUTH_KEY, sessionAuthKey);
                                    getMessage().setAuthKey(sessionAuthKey);
                                }


                                session.touch();

                                client.setProtocal(protocal);
                                client.setActive(true);
                                client.setCleanSession(cleanSession);
                                client.setSessionId(session.getId());
                                client.setConnectionId(connectionID);

                                getDatastore().saveClient(client);

                                observable.onNext(client);
                                observable.onCompleted();

                            } catch (NoSuchAlgorithmException | AuthenticationException e) {
                                observable.onError(e);
                            }
                        }, observable::onError
                );


            } catch (Exception e) {
                observable.onError(e);
            }

        });
    }

    private String generateMAC() throws NoSuchAlgorithmException {

        KeyGenerator hmacSHA2 = KeyGenerator.getInstance("HmacSHA256");
        SecretKey secretKey = hmacSHA2.generateKey();
        byte[] bytes = secretKey.getEncoded();

        return Base64.getEncoder().encodeToString(bytes);
    }

    private String processUsernameForPartition(String username) {

        Matcher matcher = usernamePartitionPattern.matcher(username);
        if (matcher.matches()) {
            return matcher.group("partition");
        } else {
            return "";
        }

    }


}
