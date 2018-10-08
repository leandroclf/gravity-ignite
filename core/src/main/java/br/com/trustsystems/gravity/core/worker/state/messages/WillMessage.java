package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.data.IdKeyComposer;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;
import java.nio.ByteBuffer;

public final class WillMessage extends IOTMessage implements IdKeyComposer {

    public static final String MESSAGE_TYPE = "WILL";
    private final boolean retain;
    private final int qos;
    private final String topic;
    @QuerySqlField(index = true)
    private String partition;
    @QuerySqlField(index = true)
    private String clientId;
    private Serializable payload;


    private WillMessage(boolean retain, int qos, String topic, String payload) {

        setMessageType(MESSAGE_TYPE);
        this.retain = retain;
        this.qos = qos;
        this.topic = topic;
        setPayload(payload);
    }

    public static WillMessage from(boolean retain, int qos, String topic, String payload) {
        return new WillMessage(retain, qos, topic, payload);
    }

    public static String getWillKey(String partition, String clientId) {

        return String.format("%s-%s", partition, clientId);
    }

    public boolean isRetain() {
        return retain;
    }

    public int getQos() {
        return qos;
    }

    public String getTopic() {
        return topic;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Serializable getPayload() {
        return payload;
    }

    public void setPayload(Serializable payload) {
        this.payload = payload;
    }

    public PublishMessage toPublishMessage() {

        byte[] willPayloadBytes = ((String) getPayload()).getBytes();
        ByteBuffer willByteBuffer = ByteBuffer.wrap(willPayloadBytes);

        long messageId = 1;
        if (getQos() > 0) {
            messageId = PublishMessage.ID_TO_FORCE_GENERATION_ON_SAVE;
        }

        //TODO: generate sequence for will message id
        return PublishMessage.from(
                messageId, false, getQos(),
                isRetain(), getTopic(),
                willByteBuffer, true
        );
    }

    @Override
    public Serializable generateIdKey() throws UnRetriableException {

        if (null == getClientId()) {
            throw new UnRetriableException(" Client Id has to be non null");
        }

        return WillMessage.getWillKey(getPartition(), getClientId());

    }
}
