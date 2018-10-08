package br.com.trustsystems.gravity.datastore.ignitecache.internal.impl;

import br.com.trustsystems.gravity.core.worker.state.messages.WillMessage;
import br.com.trustsystems.gravity.datastore.ignitecache.internal.AbstractHandler;
import org.apache.commons.configuration.Configuration;

public class WillHandler extends AbstractHandler<WillMessage> {

    public static final String CONFIG_IGNITECACHE_WILL_CACHE_NAME = "config.ignitecache.will.cache.name";
    public static final String CONFIG_IGNITECACHE_WILL_CACHE_NAME_VALUE_DEFAULT = "gravity_will_cache";


    @Override
    public void configure(Configuration configuration) {


        String cacheName = configuration.getString(CONFIG_IGNITECACHE_WILL_CACHE_NAME, CONFIG_IGNITECACHE_WILL_CACHE_NAME_VALUE_DEFAULT);
        setCacheName(cacheName);

    }

}
