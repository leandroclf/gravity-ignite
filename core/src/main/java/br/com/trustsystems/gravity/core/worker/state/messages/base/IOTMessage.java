package br.com.trustsystems.gravity.core.worker.state.messages.base;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;
import java.util.UUID;

public class IOTMessage implements Serializable {

    private UUID nodeId;
    private String cluster;
    private String authKey;
    private Serializable connectionId;

    @QuerySqlField(orderedGroups = {
            @QuerySqlField.Group(name = "partition_clientid_msgid_inbound_idx", order = 4)
    })
    private long messageId;

    private String messageType;

    private Protocol protocal;

    private Serializable sessionId;

    public Serializable getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Serializable connectionId) {
        this.connectionId = connectionId;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public Serializable getSessionId() {
        return sessionId;
    }

    public void setSessionId(Serializable sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Protocol getProtocol() {
        return protocal;
    }

    public void setProtocal(Protocol protocal) {
        this.protocal = protocal;
    }

    public void copyBase(IOTMessage iotMessage) {

        setProtocal(iotMessage.getProtocol());
        setSessionId(iotMessage.getSessionId());
        setAuthKey(iotMessage.getAuthKey());
        setConnectionId(iotMessage.getConnectionId());
        setNodeId(iotMessage.getNodeId());
        setCluster(iotMessage.getCluster());

    }


    @Override
    public String toString() {
        return getClass().getName() + '[' + "messageId=" + getMessageId() + ']';
    }
}
