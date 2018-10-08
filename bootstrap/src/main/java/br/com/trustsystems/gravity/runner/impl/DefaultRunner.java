package br.com.trustsystems.gravity.runner.impl;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.runner.ResourceService;
import br.com.trustsystems.gravity.runner.Runner;
import br.com.trustsystems.gravity.system.BaseSystemHandler;
import br.com.trustsystems.gravity.system.SystemInitializer;
import br.com.trustsystems.gravity.system.handler.ConfigHandler;
import br.com.trustsystems.gravity.system.handler.LogHandler;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DefaultRunner extends ResourceService implements Runner {


    private static final Logger log = LoggerFactory.getLogger(DefaultRunner.class);

    private final CountDownLatch _latch = new CountDownLatch(1);

    public CountDownLatch get_latch() {
        return _latch;
    }

    public void infiniteWait() {

        log.trace(" infiniteWait : application entering an infinite wait state.");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                terminate();
            }
        });

        try {
            get_latch().await();
        } catch (InterruptedException e) {
            log.warn(" infiniteWait : ", e);
        }

    }

    public void stopInfiniteWait() {

        log.trace(" stopInfiniteWait : application leaving the infinite wait state.");

        if (get_latch() != null) {
            get_latch().countDown();
        }
    }


    @Override
    public void init() throws UnRetriableException {

        log.trace(" init : initializing system configurations");

        CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(new SystemConfiguration());

        setConfiguration(configuration);

        for (ConfigHandler configHandler : getConfigurationSetLoader()) {

            log.debug(" init : found the configuration handler {} ", configHandler);

            Configuration newConfigs = configHandler.populateConfiguration(getConfiguration());
            setConfiguration(newConfigs);
        }


        for (LogHandler logHandler : getLogSetLoader()) {

            log.debug(" init : Configuring logging using handler {} ", logHandler);

            logHandler.configure(getConfiguration());

        }

    }


    @Override
    public void start() throws UnRetriableException {

        log.info(" start : Initiating operations of the whole system.");

        List<BaseSystemHandler> baseSystemHandlerList = getSystemBaseSetLoader();

        for (BaseSystemHandler baseSystemHandler : baseSystemHandlerList) {

            log.info(" start : found system handler {} ", baseSystemHandler);
            baseSystemHandler.configure(getConfiguration());

        }

        SystemInitializer systemInitializer = getSystemInitializer();
        systemInitializer.configure(getConfiguration());
        systemInitializer.systemInitialize(baseSystemHandlerList);

        infiniteWait();
    }

    @Override
    public void terminate() {

        log.info(" terminate : Terminating operations system wide.");


        for (BaseSystemHandler baseSystemHandler : getReversedSystemBaseSetLoader()) {

            log.info(" terminate : Initiating clean exit for system handler {} ", baseSystemHandler);
            baseSystemHandler.terminate();

        }

        stopInfiniteWait();
    }
}
