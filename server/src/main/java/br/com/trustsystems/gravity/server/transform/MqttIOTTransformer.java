package br.com.trustsystems.gravity.server.transform;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;

public interface MqttIOTTransformer<T> {

    IOTMessage toIOTMessage(T serverMessage);

}
