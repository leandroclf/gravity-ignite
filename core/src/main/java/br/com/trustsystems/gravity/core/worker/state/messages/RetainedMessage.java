package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.data.IdKeyComposer;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;
import java.nio.ByteBuffer;

public final class RetainedMessage extends IOTMessage implements IdKeyComposer {

    @QuerySqlField(orderedGroups={
            @QuerySqlField.Group(name = "partition_topicfilterId_idx", order = 0)
    })
    private String partition;

    @QuerySqlField(orderedGroups={
            @QuerySqlField.Group(name = "partition_topicfilterId_idx", order = 2)
    })
    private long topicFilterId;

    @QuerySqlField
    private int qos;

    @QuerySqlField
    private String topic;

    private Serializable payload;


    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public long getTopicFilterId() {
        return topicFilterId;
    }

    public void setTopicFilterId(long topicFilterId) {
        this.topicFilterId = topicFilterId;
    }

    public Serializable getPayload() {
        return payload;
    }

    public void setPayload(Serializable payload) {
        this.payload = payload;
    }

    public PublishMessage toPublishMessage() {

        ByteBuffer byteBuffer = ByteBuffer.wrap((byte[]) getPayload());

        int messageId = (int)(getTopicFilterId() % (Short.MAX_VALUE * 2));

        return  PublishMessage.from(messageId, false, getQos(), false, getTopic(), byteBuffer, false);
    }
    public static RetainedMessage from(String partition, long topicFilterId, PublishMessage publishMessage) {

        RetainedMessage retainedMessage = new RetainedMessage();
        retainedMessage.setMessageId(publishMessage.getMessageId());
        retainedMessage.setPartition(partition);
        retainedMessage.setTopicFilterId(topicFilterId);
        retainedMessage.setQos(publishMessage.getQos());
        retainedMessage.setTopic(publishMessage.getTopic());
        retainedMessage.setPayload(publishMessage.getPayload());

        return retainedMessage;
    }

    public static String createKey(String partition, long topicFilterId){
        return String.format("%s-%d", partition, topicFilterId );
    }
    @Override
    public Serializable generateIdKey() throws UnRetriableException{

        if (getTopicFilterId() <= 0 && getMessageId() <= 0) {
            throw new UnRetriableException(" Retained messages are stored only if they have a topic filter id and an Id");
        }

        return RetainedMessage.createKey(getPartition(), getTopicFilterId());
    }

}
