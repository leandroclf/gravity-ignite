package br.com.trustsystems.gravity.core;

import br.com.trustsystems.gravity.core.init.EventersInitializer;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.system.BaseSystemHandler;

import java.util.List;

public class DefaultSystemInitializer extends EventersInitializer {

    @Override
    public void systemInitialize(List<BaseSystemHandler> baseSystemHandlerList) throws UnRetriableException {

        super.systemInitialize(baseSystemHandlerList);

        //Perform flagging off for system plugins.
        startEventers();
        startDataStores();
        startWorkers();
        startServers();

    }


}
