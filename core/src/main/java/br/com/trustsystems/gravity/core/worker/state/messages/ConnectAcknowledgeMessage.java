package br.com.trustsystems.gravity.core.worker.state.messages;


import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;

public final class ConnectAcknowledgeMessage extends IOTMessage {

    public static final String MESSAGE_TYPE = "CONNACK";

    private final boolean dup;
    private final int qos;
    private final boolean retain;
    private final int keepAliveTime;
    private final MqttConnectReturnCode returnCode;


    public static ConnectAcknowledgeMessage from(boolean dup, int qos, boolean retain, int keepAliveTime, MqttConnectReturnCode returnCode) {
        return new ConnectAcknowledgeMessage(dup, qos, retain, keepAliveTime, returnCode);
    }

    private ConnectAcknowledgeMessage(boolean dup, int qos, boolean retain, int keepAliveTime, MqttConnectReturnCode returnCode) {

        setMessageType(MESSAGE_TYPE);
        this.dup = dup;
        this.qos = qos;
        this.retain = retain;
        this.keepAliveTime = keepAliveTime;
        this.returnCode = returnCode;
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

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public MqttConnectReturnCode getReturnCode() {
        return returnCode;
    }


    @Override
    public String toString() {

        return getClass().getName() + '['
                + "messageId=" + getMessageId() +","
                + "sessionId=" + getSessionId() +","
                + "keepAlive=" + getKeepAliveTime() +","
                + "returnCode=" + getReturnCode() +","
                +  ']';
    }
}
