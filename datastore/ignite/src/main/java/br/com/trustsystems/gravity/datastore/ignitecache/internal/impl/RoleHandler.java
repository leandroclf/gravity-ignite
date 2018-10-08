package br.com.trustsystems.gravity.datastore.ignitecache.internal.impl;

import br.com.trustsystems.gravity.datastore.ignitecache.internal.AbstractHandler;
import br.com.trustsystems.gravity.security.realm.state.IOTRole;
import org.apache.commons.configuration.Configuration;

public class RoleHandler extends AbstractHandler<IOTRole> {


    public static final String CONFIG_IGNITECACHE_ROLE_CACHE_NAME = "config.ignitecache.role.cache.name";
    public static final String CONFIG_IGNITECACHE_ROLE_CACHE_NAME_VALUE_DEFAULT = "gravity_role_cache";


    @Override
    public void configure(Configuration configuration) {


        String cacheName = configuration.getString(CONFIG_IGNITECACHE_ROLE_CACHE_NAME, CONFIG_IGNITECACHE_ROLE_CACHE_NAME_VALUE_DEFAULT);
        setCacheName(cacheName);

    }
}
