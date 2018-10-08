package br.com.trustsystems.gravity.datastore.ignitecache.internal.impl;

import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.datastore.ignitecache.internal.AbstractHandler;
import org.apache.commons.configuration.Configuration;

public class ClientHandler extends AbstractHandler<Client> {


    public static final String CONFIG_IGNITECACHE_CLIENT_CACHE_NAME = "config.ignitecache.client.cache.name";
    public static final String CONFIG_IGNITECACHE_CLIENT_CACHE_NAME_VALUE_DEFAULT = "gravity_client_cache";



    @Override
    public void configure(Configuration configuration) {


        String cacheName = configuration.getString(CONFIG_IGNITECACHE_CLIENT_CACHE_NAME, CONFIG_IGNITECACHE_CLIENT_CACHE_NAME_VALUE_DEFAULT);
        setCacheName(cacheName);

    }
}
