package br.com.trustsystems.gravity.core.worker.state.models;


import br.com.trustsystems.gravity.core.modules.Datastore;
import br.com.trustsystems.gravity.core.worker.state.Messenger;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.WillMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.Protocol;
import br.com.trustsystems.gravity.data.IdKeyComposer;
import br.com.trustsystems.gravity.exceptions.RetriableException;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import rx.Observable;

import java.io.Serializable;
import java.util.UUID;

public class Client implements IdKeyComposer, Serializable {

    @QuerySqlField(orderedGroups = {@QuerySqlField.Group(
            name = "partition_clientid_idx", order = 0)})
    private String partition;

    @QuerySqlField(orderedGroups = {@QuerySqlField.Group(
            name = "partition_clientid_idx", order = 2)})
    private String clientId;

    @QuerySqlField()
    private Serializable sessionId;

    @QuerySqlField()
    private String connectedCluster;

    @QuerySqlField()
    private UUID connectedNode;

    @QuerySqlField()
    private Serializable connectionId;

    @QuerySqlField()
    private boolean active;

    private boolean cleanSession;

    private Protocol protocal;

    private String protocalData;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Serializable getSessionId() {
        return sessionId;
    }

    public void setSessionId(Serializable sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getConnectedNode() {
        return connectedNode;
    }

    public void setConnectedNode(UUID connectedNode) {
        this.connectedNode = connectedNode;
    }

    public String getConnectedCluster() {
        return connectedCluster;
    }

    public void setConnectedCluster(String connectedCluster) {
        this.connectedCluster = connectedCluster;
    }

    public Serializable getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Serializable connectionId) {
        this.connectionId = connectionId;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public Protocol getProtocal() {
        return protocal;
    }

    public void setProtocal(Protocol protocal) {
        this.protocal = protocal;
    }

    public String getProtocalData() {
        return protocalData;
    }

    public void setProtocalData(String protocalData) {
        this.protocalData = protocalData;
    }

    /**

     */
    public void internalPublishMessage(Messenger messenger, PublishMessage publishMessage) throws RetriableException {

        messenger.publish(getPartition(), publishMessage);
    }


    public Observable<WillMessage> getWill(Datastore datastore) {
        String willKey = WillMessage.getWillKey(getPartition(), getClientId());
        return datastore.getWill(willKey);
    }


    @Override
    public Serializable generateIdKey() throws UnRetriableException {

        if (null == getClientId()) {
            throw new UnRetriableException(" Client Id has to be non null");
        }

        return String.format("%s-%s", getPartition(), getClientId());
    }

    public <T extends IOTMessage> T copyTransmissionData(T iotMessage) {

        iotMessage.setSessionId(getSessionId());
        iotMessage.setProtocal(getProtocal());
        iotMessage.setConnectionId(getConnectionId());
        iotMessage.setNodeId(getConnectedNode());
        iotMessage.setCluster(getConnectedCluster());

        if (iotMessage instanceof PublishMessage) {
            PublishMessage publishMessage = (PublishMessage) iotMessage;
            publishMessage.setPartition(getPartition());
            publishMessage.setClientId(getClientId());
        }


        return iotMessage;
    }
}
