package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SubscribeMessage extends IOTMessage {

    public static final String MESSAGE_TYPE = "SUBSCRIBE";

    private final boolean dup;
    private final int qos;
    private final boolean retain;
    private final List<Map.Entry<String, Integer>> topicFilterList = new ArrayList<>();
    private String receptionUrl;


    private SubscribeMessage(long messageId, boolean dup, int qos, boolean retain) {

        setMessageType(MESSAGE_TYPE);
        setMessageId(messageId);
        this.dup = dup;
        this.qos = qos;
        this.retain = retain;

    }

    public static SubscribeMessage from(long messageId, boolean dup, int qos, boolean retain) {
        if (messageId < 1) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: > 1)");
        }
        return new SubscribeMessage(messageId, dup, qos, retain);
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


    public List<Map.Entry<String, Integer>> getTopicFilterList() {
        return topicFilterList;
    }

    public String getReceptionUrl() {
        return receptionUrl;
    }

    public void setReceptionUrl(String receptionUrl) {
        this.receptionUrl = receptionUrl;
    }
}
