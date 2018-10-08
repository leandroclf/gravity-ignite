package br.com.trustsystems.gravity.core.init;

import br.com.trustsystems.gravity.core.modules.Datastore;
import br.com.trustsystems.gravity.core.security.DefaultSecurityHandler;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.system.BaseSystemHandler;
import org.apache.commons.configuration.Configuration;
import org.apache.ignite.cluster.ClusterGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public abstract class DatastoresInitializer extends WorkersInitializer {


    public static final String CORE_CONFIG_ENGINE_DATASTORE_IS_ENABLED = "core.config.engine.datastore.is.enabled";
    public static final boolean CORE_CONFIG_ENGINE_DATASTORE_IS_ENABLED_DEFAULT_VALUE = true;

    public static final String CORE_CONFIG_ENGINE_DATASTORE_CLASS_NAME = "core.config.engine.datastore.class.name";
    public static final String CORE_CONFIG_ENGINE_DATASTORE_CLASS_NAME_DEFAULT_VALUE = "br.com.trustsystems.gravity.datastore.ignitecache.IgniteDatastore";

    public static final String CORE_CONFIG_DATASTORE_PARTITION_BASED_ON_USERNAME = "core.config.datastore.partition.based.on.username";
    public static final boolean CORE_CONFIG_DATASTORE_PARTITION_BASED_ON_USERNAME_DEFAULT_VALUE = false;

    private final DefaultSecurityHandler securityHandler = new DefaultSecurityHandler();

    private boolean datastoreEngineEnabled;

    private String datastoreClassName;
    private List<Datastore> datastoreList = new ArrayList<>();
    private Datastore activeDatastore;

    public boolean isDatastoreEngineEnabled() {
        return datastoreEngineEnabled;
    }

    public void setDatastoreEngineEnabled(boolean datastoreEngineEnabled) {
        this.datastoreEngineEnabled = datastoreEngineEnabled;
    }

    public String getDatastoreClassName() {
        return datastoreClassName;
    }

    public void setDatastoreClassName(String datastoreClassName) {
        this.datastoreClassName = datastoreClassName;
    }

    public List<Datastore> getDatastoreList() {
        return datastoreList;
    }

    public Datastore getActiveDatastore() {
        return activeDatastore;
    }

    public void setActiveDatastore(Datastore activeDatastore) {
        this.activeDatastore = activeDatastore;
    }


    public void startDataStores() throws UnRetriableException {

        log.debug(" startDataStores : Starting the system datastores");

        if (isDatastoreEngineEnabled() && getDatastoreList().isEmpty()) {
            log.warn("List of datastore plugins is empty");
            throw new UnRetriableException(" System expects atleast one datastore plugin to be configured.");
        }

        for (Datastore datastore : getDatastoreList()) {

            if (validateDatastoreCanBeLoaded(datastore)) {

                datastore.setIgnite(getIgnite());

                ClusterGroup datastoreCluster = getIgnite().cluster().forAttribute("ROLE", getExecutorDatastoreName());
                ExecutorService executorService = getIgnite().executorService(datastoreCluster);
                datastore.setExecutorService(executorService);

                datastore.initiate();

                setActiveDatastore(datastore);
                break;
            }
        }


        //Assign our workers the active datastore.
        getWorkerList().forEach(worker -> {

            worker.setDatastore(getActiveDatastore());
            securityHandler.getSessionListenerList().add(worker);
        });


        //Initialize security.
        securityHandler.setIotAccountDatastore(getActiveDatastore());
        //Perform initialization of the security system too.
        securityHandler.initiate(getIgnite());

        String securityFile = securityHandler.getSecurityIniPath();
        securityHandler.createSecurityManager(securityFile);
    }

    /**
     * Simple method that does validatations to determine
     * if the datastore plugin matches whatever is in the config files then it
     * can be loaded.
     *
     * @param datastore
     * @return
     */
    private boolean validateDatastoreCanBeLoaded(Datastore datastore) {
        if (null != datastore) {

            return datastore.getClass().getName().equals(getDatastoreClassName());
        }
        return false;
    }


    protected void classifyBaseHandler(BaseSystemHandler baseSystemHandler) {

        if (baseSystemHandler instanceof Datastore) {

            log.debug(" classifyBaseHandler : found the datastore {}", baseSystemHandler);

            if (isDatastoreEngineEnabled()) {

                datastoreList.add((Datastore) baseSystemHandler);
                log.info(" classifyBaseHandler : storing the datastore : {} for use as active plugin", baseSystemHandler);

            } else {

                log.info(" classifyBaseHandler : datastore {} is disabled ", baseSystemHandler);

            }
        } else {
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


        boolean configDatastoreEnabled = configuration.getBoolean(CORE_CONFIG_ENGINE_DATASTORE_IS_ENABLED, CORE_CONFIG_ENGINE_DATASTORE_IS_ENABLED_DEFAULT_VALUE);

        log.debug(" configure : The datastore function is configured to be enabled [{}]", configDatastoreEnabled);

        setDatastoreEngineEnabled(configDatastoreEnabled);

        String configDatastoreClassName = configuration.getString(CORE_CONFIG_ENGINE_DATASTORE_CLASS_NAME, CORE_CONFIG_ENGINE_DATASTORE_CLASS_NAME_DEFAULT_VALUE);

        log.debug(" configure : The datastore class to be loaded by the system is {}", configDatastoreClassName);

        setDatastoreClassName(configDatastoreClassName);

        super.configure(configuration);

        securityHandler.configure(configuration);

    }


}
