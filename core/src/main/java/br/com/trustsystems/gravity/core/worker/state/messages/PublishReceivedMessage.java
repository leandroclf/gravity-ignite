package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

public final class PublishReceivedMessage extends IOTMessage {

    public static final String MESSAGE_TYPE = "PUBREC";

    private final int qos = 0;

    public static PublishReceivedMessage from(long messageId) {
        if (messageId < 1 ) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: > 1)");
        }
        return new PublishReceivedMessage(messageId);
    }

    private PublishReceivedMessage(long messageId) {

        setMessageType(MESSAGE_TYPE);
        setMessageId(messageId);
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
