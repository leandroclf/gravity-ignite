package br.com.trustsystems.gravity.system.handler;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.commons.configuration.Configuration;

import java.io.File;

public interface LogHandler {

    String SYSTEM_CONFIG_LOGGING_LOG_CONFIG_FILE = "system.config.logging.log.config.file";
    String SYSTEM_CONFIG_LOGGING_LOG_CONFIG_FILE_DEFAULT_VALUE = "log4j.properties";

    String SYSTEM_CONFIG_LOGGING_LOG_CONFIG_DIRECTORY = "system.config.logging.log.config.directory";
    String SYSTEM_CONFIG_LOGGING_LOG_CONFIG_DIRECTORY_DEFAULT_VALUE = "";

    String DEFAULT_CONFIG_DIRECTORY = ".." + File.separator + "conf";


    /**
     * <code>configure</code> Allows the system to supply configurations
     * from other modules and use these settings to configure the logging system.
     * It is upto the implementation to ensure the settings it expects are supplied
     * in the configuration by populating the necessary config settings.
     *
     * @param configuration
     * @throws UnRetriableException
     */
    void configure(Configuration configuration) throws UnRetriableException;
}
