package br.com.trustsystems.gravity.system;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.commons.configuration.Configuration;

import java.io.Serializable;

public interface BaseSystemHandler extends Comparable<BaseSystemHandler>, Serializable{

    /**
     * <code>configure</code> allows the base system to configure itself by getting
     * all the settings it requires and storing them internally. The plugin is only expected to
     * pick the settings it has registered on the configuration file for its particular use.
     * @param configuration
     * @throws UnRetriableException
     */
    void configure(Configuration configuration) throws UnRetriableException;

    /**
     * <code>initiate</code> starts the operations of this system handler.
     * All excecution code for the plugins is expected to begin at this point.
     *
     * @throws UnRetriableException
     */
    void initiate() throws UnRetriableException;

    /**
     * <code>terminate</code> halts excecution of this plugin.
     * This provides a clean way to exit /stop operations of this particular plugin.
     */
    void terminate();
}
