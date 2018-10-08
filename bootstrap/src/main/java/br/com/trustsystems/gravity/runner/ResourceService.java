package br.com.trustsystems.gravity.runner;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.system.BaseSystemHandler;
import br.com.trustsystems.gravity.system.SystemInitializer;
import br.com.trustsystems.gravity.system.handler.ConfigHandler;
import br.com.trustsystems.gravity.system.handler.LogHandler;
import org.apache.commons.configuration.Configuration;

import java.util.*;

public abstract class ResourceService {


    private Configuration configuration;

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public ServiceLoader<ConfigHandler> getConfigurationSetLoader() {

        return ServiceLoader.load(ConfigHandler.class);
    }

    public ServiceLoader<LogHandler> getLogSetLoader() {

        return ServiceLoader.load(LogHandler.class);
    }

    public List<BaseSystemHandler> getSystemBaseSetLoader() {

        List<BaseSystemHandler> listBaseSystemHandler = new ArrayList<>();
        for (BaseSystemHandler baseSystemHandler : ServiceLoader.load(BaseSystemHandler.class))
            listBaseSystemHandler.add(baseSystemHandler);

        Collections.sort(listBaseSystemHandler);

        return listBaseSystemHandler;
    }


    public List<BaseSystemHandler> getReversedSystemBaseSetLoader() {

        List<BaseSystemHandler> listBaseSystemHandler = getSystemBaseSetLoader();

        Collections.reverse(listBaseSystemHandler);

        return listBaseSystemHandler;
    }


    public SystemInitializer getSystemInitializer() throws UnRetriableException {

        Iterator<SystemInitializer> systemInitializerIterator = ServiceLoader.load(SystemInitializer.class).iterator();

        if (systemInitializerIterator.hasNext())
            return systemInitializerIterator.next();
        else
            throw new UnRetriableException("A plugin supplying the system initializer: br.com.trustsystems.gravity.system.SystemInitializer is missing");
    }

}
