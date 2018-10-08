package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

import java.util.List;

public final class UnSubscribeMessage extends IOTMessage {

    public static final String MESSAGE_TYPE = "UNSUBSCRIBE";

    private final boolean dup;
    private final int qos;
    private final boolean retain;

    private final List<String> topicFilterList;


    private UnSubscribeMessage(long messageId, boolean dup, int qos, boolean retain, List<String> topicFilterList) {

        setMessageType(MESSAGE_TYPE);
        setMessageId(messageId);
        this.dup = dup;
        this.qos = qos;
        this.retain = retain;
        this.topicFilterList = topicFilterList;

    }

    public static UnSubscribeMessage from(long messageId, boolean dup, int qos, boolean retain, List<String> topicFilterList) {
        if (messageId < 1) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: > 1)");
        }
        return new UnSubscribeMessage(messageId, dup, qos, retain, topicFilterList);
    }

    public boolean isDup() {
        return dup;
    }

    public int getQos() {
        return qos;
    }

    public boolean isRetain() {
        return retain;
    }


    public List<String> getTopicFilterList() {
        return topicFilterList;
    }
}
