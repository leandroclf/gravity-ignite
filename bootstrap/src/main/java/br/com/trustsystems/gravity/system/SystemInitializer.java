package br.com.trustsystems.gravity.system;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.commons.configuration.Configuration;

import java.util.List;

public interface SystemInitializer {

    /**
     * <code>configure</code> allows the initializer to configure its self
     * Depending on the implementation conditional operation can be allowed
     * So as to make the system instance more specialized.
     * <p>
     * For example: via the configurations the implementation may decide to
     * shutdown backend services and it just works as a server application to receive
     * and route requests to the workers which are in turn connected to the backend/datastore servers...
     *
     * @param configuration
     * @throws UnRetriableException
     */

    void configure(Configuration configuration) throws UnRetriableException;

    void systemInitialize(List<BaseSystemHandler> baseSystemHandlerList) throws UnRetriableException;
}
