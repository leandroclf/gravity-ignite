package br.com.trustsystems.gravity.system.handler.impl;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.system.ResourceFileUtil;
import br.com.trustsystems.gravity.system.handler.LogHandler;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DefaultLogHandler implements LogHandler {


    private static final Logger log = LoggerFactory.getLogger(DefaultLogHandler.class);


    @Override
    public void configure(Configuration configuration) throws UnRetriableException {


        String logsConfigFile = SYSTEM_CONFIG_LOGGING_LOG_CONFIG_FILE_DEFAULT_VALUE;
        String logsConfigDirectory = System.getProperty("gravity.default.path.conf", DEFAULT_CONFIG_DIRECTORY);

        if (null != configuration) {
            logsConfigFile = configuration.getString(SYSTEM_CONFIG_LOGGING_LOG_CONFIG_FILE, SYSTEM_CONFIG_LOGGING_LOG_CONFIG_FILE_DEFAULT_VALUE);
            logsConfigDirectory = configuration.getString(SYSTEM_CONFIG_LOGGING_LOG_CONFIG_DIRECTORY, logsConfigDirectory);
        }

        String logsConfigFilePath = logsConfigDirectory + File.separator + logsConfigFile;

        log.debug(" configure : path to logging configs is {} .", logsConfigFilePath);

        File logConfigurationFile = new File(logsConfigFilePath);

        if (!logConfigurationFile.exists()) {

            logConfigurationFile = ResourceFileUtil.getFileFromResource(getClass(), logsConfigFile);

        }

        log.debug(" configure : File with logging configs is {} .", logConfigurationFile);


        PropertyConfigurator.configure(logConfigurationFile.getAbsolutePath());

    }


}
