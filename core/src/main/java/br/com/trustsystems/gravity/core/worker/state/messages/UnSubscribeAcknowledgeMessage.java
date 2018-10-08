package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

public final class UnSubscribeAcknowledgeMessage extends IOTMessage {

    public static final String MESSAGE_TYPE = "UNSUBACK";

    private final boolean dup;
    private final int qos = 0;
    private final boolean retain;

    public static UnSubscribeAcknowledgeMessage from(long messageId) {
        if (messageId < 1 ) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: > 1)");
        }

        return new UnSubscribeAcknowledgeMessage(messageId, false,false);
    }

    private UnSubscribeAcknowledgeMessage(long messageId, boolean dup, boolean retain) {

        setMessageType(MESSAGE_TYPE);
        setMessageId(messageId);

        this.dup = dup;
        this.retain = retain;

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

    @Override
    public String toString() {
        return new StringBuilder(getClass().getName())
                .append('[')
                .append("qos=").append(getQos())
                .append(']')
                .toString();
    }
}
