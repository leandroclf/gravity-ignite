package br.com.trustsystems.gravity.core.worker.state.messages;


import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

public final class CompleteMessage extends IOTMessage {

    public static final String MESSAGE_TYPE = "PUBCOMP";

    private final int qos =0;


    public static CompleteMessage from(long messageId) {
        if (messageId < 1) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: > 1)");
        }
        return new CompleteMessage(messageId);
    }

    private CompleteMessage(long messageId) {
        setMessageType(MESSAGE_TYPE);
        setMessageId(messageId);
    }




    public int getQos() {
        return qos;
    }

    @Override
    public String toString() {
        return getClass().getName() + '[' + "messageId=" + getMessageId() + ']';
    }
}
