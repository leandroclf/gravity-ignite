
package br.com.trustsystems.gravity.server;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.commons.configuration.Configuration;

import java.io.Serializable;

public interface ServerInterface<T> {

    void configure(Configuration configuration) throws UnRetriableException;

    void initiate() throws UnRetriableException;

    void terminate();

    void pushToClient(Serializable connectionId, T message);

    void postProcess(IOTMessage ioTMessage);
}
