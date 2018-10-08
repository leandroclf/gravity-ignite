package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

import java.util.List;

public final class SubscribeAcknowledgeMessage extends IOTMessage {

    public static final String MESSAGE_TYPE = "SUBACK";

    private final boolean dup;
    private final int qos =0;
    private final boolean retain;
    private final List<Integer> grantedQos;

    public static SubscribeAcknowledgeMessage from(long messageId, List<Integer> grantedQos) {
        if (messageId < 1 ) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: > 1)");
        }

        return new SubscribeAcknowledgeMessage( messageId, false, false, grantedQos);
    }

    private SubscribeAcknowledgeMessage(long messageId, boolean dup, boolean retain, List<Integer> grantedQos) {

        setMessageType(MESSAGE_TYPE);
        setMessageId(messageId);
        this.dup = dup;
        this.retain = retain;
        this.grantedQos = grantedQos;

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

    public List<Integer> getGrantedQos() {
        return grantedQos;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getName())
                .append('[')
                .append("grantedQos=").append(getGrantedQos())
                .append(']')
                .toString();
    }
}
