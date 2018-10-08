package br.com.trustsystems.gravity.core.modules.base.server;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import rx.Observable;

import java.util.UUID;

public interface ServerRouter extends Observable.OnSubscribe<IOTMessage> {

    void initiate();

    void route(String cluster, UUID nodeId, IOTMessage message);
}
