package br.com.trustsystems.gravity.core.worker.state.messages.base;

public enum Protocol {

    MQTT(true), HTTP(false);

    private final boolean persistent;

    Protocol(boolean persistent){
        this.persistent = persistent;
    }

    public boolean isPersistent(){
        return persistent;
    }

    public boolean isNotPersistent(){
        return !persistent;
    }
}
