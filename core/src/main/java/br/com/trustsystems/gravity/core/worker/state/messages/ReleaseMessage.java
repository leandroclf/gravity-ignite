package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

public final class ReleaseMessage extends IOTMessage {

    public static final String MESSAGE_TYPE = "PUBREL";

    private final boolean dup;
    private final int qos = 1;

    public static ReleaseMessage from(long messageId, boolean dup) {
        if (messageId < 1 ) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: > 1)");
        }
        return new ReleaseMessage(messageId, dup);
    }

    private ReleaseMessage(long messageId, boolean dup) {

        setMessageType(MESSAGE_TYPE);
        setMessageId(messageId);
        this.dup = dup;

    }

    public boolean isDup() {
        return dup;
    }

    public int getQos() {
        return qos;
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
