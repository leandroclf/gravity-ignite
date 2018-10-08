package br.com.trustsystems.gravity.core.init;

import br.com.trustsystems.gravity.core.modules.Worker;
import br.com.trustsystems.gravity.core.worker.state.Messenger;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.system.BaseSystemHandler;
import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

public abstract class WorkersInitializer extends ServersInitializer {


    public static final String CORE_CONFIG_ENGINE_WORKER_IS_ENABLED = "core.config.engine.worker.is.enabled";
    public static final boolean CORE_CONFIG_ENGINE_WORKER_IS_ENABLED_DEFAULT_VALUE = true;

    private boolean workerEngineEnabled;

    private boolean annonymousLoginEnabled;

    private String annonymousLoginUsername;

    private String annonymousLoginPassword;



    private List<Worker> workerList = new ArrayList<>();

    public boolean isWorkerEngineEnabled() {
        return workerEngineEnabled;
    }

    public void setWorkerEngineEnabled(boolean workerEngineEnabled) {
        this.workerEngineEnabled = workerEngineEnabled;
    }

    public boolean isAnnonymousLoginEnabled() {
        return annonymousLoginEnabled;
    }

    public void setAnnonymousLoginEnabled(boolean annonymousLoginEnabled) {
        this.annonymousLoginEnabled = annonymousLoginEnabled;
    }

    public String getAnnonymousLoginUsername() {
        return annonymousLoginUsername;
    }

    public void setAnnonymousLoginUsername(String annonymousLoginUsername) {
        this.annonymousLoginUsername = annonymousLoginUsername;
    }

    public String getAnnonymousLoginPassword() {
        return annonymousLoginPassword;
    }

    public void setAnnonymousLoginPassword(String annonymousLoginPassword) {
        this.annonymousLoginPassword = annonymousLoginPassword;
    }

    public List<Worker> getWorkerList() {
        return workerList;
    }




    /**
     * <code>startWorkers</code> Any kind of worker can be loaded on to the system.
     * This can allow for the same request to be processed in two different ways.
     * How that is usefull now is left to the person actually executing such an implementation
     * The design leaves room for creativity upto the core of IOTracah.
     *
     * Beyond multiple workers, normall operations of the system would be expected to be with
     * the default workers only. This way a true mqtt broker is allowed to be built.
     *
     * The workers expect to be pushed data from servers they handle it appropriately producing
     * all the relevant events then they return a response by pushing it to the server layer.
     *
     * @throws UnRetriableException
     */
    public void startWorkers() throws UnRetriableException {

        if(isWorkerEngineEnabled() && getWorkerList().isEmpty()) {
            log.warn("List of worker plugins is empty");
            throw new UnRetriableException(" System expects atleast one worker plugin to be configured.");
        }

        log.debug(" startWorkers : Starting the system workers");

        for (Worker worker : getWorkerList()) {
            //Link worker observable to servers.
            subscribeObserverToObservables(worker, getServerList());

            //Assign router
            worker.setServerRouter(getServerRouter());

            //Assign messenger.
            Messenger messenger = new Messenger();
            messenger.setWorker(worker);
            messenger.setDatastore(worker.getDatastore());
            worker.setMessenger(messenger);

            //Actually start our worker guy.
            worker.initiate();
        }
    }

    protected void classifyBaseHandler(BaseSystemHandler baseSystemHandler){

        if(baseSystemHandler instanceof Worker){

            log.debug(" classifyBaseHandler : found the worker {}", baseSystemHandler);

            if(isWorkerEngineEnabled()){
                log.info(" classifyBaseHandler : storing the worker : {} for use as active plugin", baseSystemHandler);
            workerList.add((Worker) baseSystemHandler);
            } else {
                log.info(" classifyBaseHandler : worker {} is disabled ", baseSystemHandler);
            }
        }
        else{
            super.classifyBaseHandler(baseSystemHandler);
        }
    }

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
    @Override
    public void configure(Configuration configuration) throws UnRetriableException {


        boolean configWorkerEnabled = configuration.getBoolean(CORE_CONFIG_ENGINE_WORKER_IS_ENABLED, CORE_CONFIG_ENGINE_WORKER_IS_ENABLED_DEFAULT_VALUE);

        log.debug(" configure : The worker function is configured to be enabled [{}]", configWorkerEnabled);

        setWorkerEngineEnabled(configWorkerEnabled);




        super.configure(configuration);

    }



}
