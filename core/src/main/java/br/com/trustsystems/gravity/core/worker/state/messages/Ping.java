package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

public final class Ping extends IOTMessage {

    public static final String MESSAGE_TYPE = "PING";

    private final boolean dup;
    private final int qos;
    private final boolean retain;

    private Ping(boolean dup, int qos, boolean retain) {

        setMessageType(MESSAGE_TYPE);
        this.dup = dup;
        this.qos = qos;
        this.retain = retain;

    }

    public static Ping from(boolean dup, int qos, boolean retain) {

        return new Ping(dup, qos, retain);
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
}
