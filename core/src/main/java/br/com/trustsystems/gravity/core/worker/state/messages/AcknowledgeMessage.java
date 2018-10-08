package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

public final class AcknowledgeMessage extends IOTMessage {

    public static final String MESSAGE_TYPE = "PUBACK";

    private final boolean dup = false;
    private final int qos = 0;
    private final boolean retain = false;

    private AcknowledgeMessage(long messageId) {

        setMessageId(messageId);
        setMessageType(MESSAGE_TYPE);

    }

    public static AcknowledgeMessage from(long messageId) {
        if (messageId < 1) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: > 1)");
        }
        return new AcknowledgeMessage(messageId);
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
                .append("messageId=").append(getMessageId())
                .append(']')
                .toString();
    }
}
