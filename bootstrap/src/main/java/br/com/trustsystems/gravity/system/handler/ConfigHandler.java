package br.com.trustsystems.gravity.system.handler;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.commons.configuration.Configuration;

import java.io.File;

public interface ConfigHandler {


    String SYSTEM_CONFIG_CONFIGURATION_FILE_NAME_DEFAULT_VALUE = "gravity.properties";

    String DEFAULT_CONFIG_DIRECTORY = ".." + File.separator + "conf";

    /**
     * All system configurations providers are loaded via spi
     * and are given the configurations the system already has.
     * They are further expected to provide their configurations in
     * and additive way. Since the order of loading the configs is not
     * guranteed all the setting keys should be uniquely identified
     * and that task is left to the implementations to enforce.
     *
     * @param configuration
     * @return
     * @throws UnRetriableException
     */

    Configuration populateConfiguration(Configuration configuration) throws UnRetriableException;
}
