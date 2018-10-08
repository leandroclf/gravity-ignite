package br.com.trustsystems.gravity.core.worker.state.messages;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

public final class DisconnectMessage extends IOTMessage {

    public static final String MESSAGE_TYPE = "DISCONNECT";

    private final boolean dirtyDisconnect;

    public DisconnectMessage(boolean dirtyDisconnect) {

        setMessageType(MESSAGE_TYPE);
        this.dirtyDisconnect = dirtyDisconnect;
    }



    public static DisconnectMessage from( boolean dirtyDisconnect) {
        return new DisconnectMessage(dirtyDisconnect);


    }

    public boolean isDirtyDisconnect() {
        return dirtyDisconnect;
    }

}
