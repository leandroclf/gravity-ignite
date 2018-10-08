package br.com.trustsystems.gravity.core.security;

import br.com.trustsystems.gravity.core.worker.state.models.IOTSession;
import br.com.trustsystems.gravity.core.worker.state.session.SessionDAO;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import br.com.trustsystems.gravity.security.IOTIniSecurityManagerFactory;
import br.com.trustsystems.gravity.security.IOTSecurityManager;
import br.com.trustsystems.gravity.security.realm.IOTAccountDatastore;
import br.com.trustsystems.gravity.system.ResourceFileUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMemoryMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicy;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

public class DefaultSecurityHandler {

    public static final String CONFIGURATION_VALUE_DEFAULT_SECURITY_FILE_NAME = "security.ini";
    public static final String SYSTEM_CONFIG_SECURITY_CONFIG_DIRECTORY = "system.config.security.config.directory";
    public static final String SYSTEM_CONFIG_SECURITY_CONFIG_DIRECTORY_DEFAULT_VALUE = "";
    public static final String CONFIG_IGNITECACHE_SESSION_CACHE_NAME = "config.ignitecache.session.cache.name";
    public static final String CONFIG_IGNITECACHE_SESSION_CACHE_NAME_VALUE_DEFAULT = "gravity_session_cache";
    public static final String CONFIG_IGNITECACHE_SESSION_ATOMIC_SEQUENCE_NAME = "config.ignitecache.session.atomic.sequence.name";
    public static final String CONFIG_IGNITECACHE_SESSION_ATOMIC_SEQUENCE_NAME_VALUE_DEFAULT = "gravity_session_atomic_sequence";
    private static final Logger log = LoggerFactory.getLogger(DefaultSecurityHandler.class);
    private final String securityFileName;
    private String securityFileDirectory;
    private String cacheName;
    private String atomicSequenceName;
    private IgniteCache<Serializable, IOTSession> sessionsCache;
    private IgniteAtomicSequence atomicSequence;

    private IOTAccountDatastore iotAccountDatastore;

    private Set<SessionListener> sessionListenerList = new HashSet<>();

    public DefaultSecurityHandler() {
        this.securityFileName = CONFIGURATION_VALUE_DEFAULT_SECURITY_FILE_NAME;
    }

    public DefaultSecurityHandler(String securityFileName) {
        this.securityFileName = securityFileName;
    }

    public String getSecurityFileName() {
        return securityFileName;
    }

    public String getSecurityFileDirectory() {
        return securityFileDirectory;
    }

    public void setSecurityFileDirectory(String securityFileDirectory) {
        this.securityFileDirectory = securityFileDirectory;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getAtomicSequenceName() {
        return atomicSequenceName;
    }

    public void setAtomicSequenceName(String atomicSequenceName) {
        this.atomicSequenceName = atomicSequenceName;
    }

    public IgniteCache<Serializable, IOTSession> getSessionsCache() {
        return sessionsCache;
    }

    public void setSessionsCache(IgniteCache<Serializable, IOTSession> sessionsCache) {
        this.sessionsCache = sessionsCache;
    }

    public IgniteAtomicSequence getAtomicSequence() {
        return atomicSequence;
    }

    public void setAtomicSequence(IgniteAtomicSequence atomicSequence) {
        this.atomicSequence = atomicSequence;
    }

    public IOTAccountDatastore getIotAccountDatastore() {
        return iotAccountDatastore;
    }

    public void setIotAccountDatastore(IOTAccountDatastore iotAccountDatastore) {
        this.iotAccountDatastore = iotAccountDatastore;
    }

    public Set<SessionListener> getSessionListenerList() {
        return sessionListenerList;
    }

    public String getSecurityIniPath() throws UnRetriableException {

        File securityFile = new File(getSecurityFileDirectory() + File.separator + getSecurityFileName());

        if (!securityFile.exists()) {

            log.warn(" getSecurityIniPath : Security file not found in the configurations directory. Falling back to the defaults");

            securityFile = ResourceFileUtil.getFileFromResource(getClass(), getSecurityFileName());

            return securityFile.getPath();

        } else {
            return securityFile.getPath();
        }

    }


    public void configure(Configuration configuration) {


        String securityFileDirectory = System.getProperty("gravity.default.path.conf", SYSTEM_CONFIG_SECURITY_CONFIG_DIRECTORY_DEFAULT_VALUE);

        securityFileDirectory = configuration.getString(SYSTEM_CONFIG_SECURITY_CONFIG_DIRECTORY, securityFileDirectory);

        setSecurityFileDirectory(securityFileDirectory);

        String cacheName = configuration.getString(CONFIG_IGNITECACHE_SESSION_CACHE_NAME, CONFIG_IGNITECACHE_SESSION_CACHE_NAME_VALUE_DEFAULT);
        setCacheName(cacheName);

        String atomicSequenceName = configuration.getString(CONFIG_IGNITECACHE_SESSION_ATOMIC_SEQUENCE_NAME, CONFIG_IGNITECACHE_SESSION_ATOMIC_SEQUENCE_NAME_VALUE_DEFAULT);
        setAtomicSequenceName(atomicSequenceName);

    }


    public void createSecurityManager(String securityFilePath) throws UnRetriableException {


        Ini ini = new Ini();
        ini.loadFromPath(securityFilePath);

        IOTIniSecurityManagerFactory iniSecurityManagerFactory = new IOTIniSecurityManagerFactory(ini, getIotAccountDatastore());

        SecurityManager securityManager = iniSecurityManagerFactory.getInstance();

        if (securityManager instanceof IOTSecurityManager) {

            //configure the security manager.
            IOTSecurityManager iotSecurityManager = (IOTSecurityManager) securityManager;
            DefaultSessionManager sessionManager = (DefaultSessionManager) iotSecurityManager.getSessionManager();


            SecurityUtils.setSecurityManager(iotSecurityManager);


            //Create our sessions DAO
            SessionDAO sessionDAO = new SessionDAO(getSessionsCache(), getAtomicSequence());
            sessionDAO.init();
            sessionManager.setSessionDAO(sessionDAO);

            sessionManager.setSessionListeners(getSessionListenerList());
            sessionManager.setSessionValidationSchedulerEnabled(true);
            sessionManager.setSessionValidationInterval(1000);


        } else {
            throw new UnRetriableException("Security manager has to be an instance of the default security manager (DefaultSecurityManager). " + securityManager.getClass().getName() + " was used instead.");
        }
    }


    public void initiate(Ignite ignite) {

        CacheConfiguration clCfg = new CacheConfiguration();

        clCfg.setName(getCacheName());
        clCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        clCfg.setCacheMode(CacheMode.PARTITIONED);
        clCfg.setIndexedTypes(String.class, IOTSession.class);
        clCfg.setMemoryMode(CacheMemoryMode.ONHEAP_TIERED);

        LruEvictionPolicy lruEvictionPolicy = new LruEvictionPolicy(5170000);
        clCfg.setEvictionPolicy(lruEvictionPolicy);

        clCfg.setSwapEnabled(true);
        ignite.createCache(clCfg);


        IgniteCache<Serializable, IOTSession> clientIgniteCache = ignite.cache(getCacheName());
        setSessionsCache(clientIgniteCache);


        long currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        IgniteAtomicSequence atomicSequence = ignite.atomicSequence(getAtomicSequenceName(), currentTime, true);
        setAtomicSequence(atomicSequence);


    }


}
