package br.com.trustsystems.gravity.datastore.ignitecache.internal.impl;

import br.com.trustsystems.gravity.datastore.ignitecache.internal.AbstractHandler;
import br.com.trustsystems.gravity.security.realm.state.IOTAccount;
import org.apache.commons.configuration.Configuration;

public class AccountHandler extends AbstractHandler<IOTAccount> {


    public static final String CONFIG_IGNITECACHE_ACCOUNT_CACHE_NAME = "config.ignitecache.account.cache.name";
    public static final String CONFIG_IGNITECACHE_ACCOUNT_CACHE_NAME_VALUE_DEFAULT = "gravity_account_cache";


    @Override
    public void configure(Configuration configuration) {


        String cacheName = configuration.getString(CONFIG_IGNITECACHE_ACCOUNT_CACHE_NAME, CONFIG_IGNITECACHE_ACCOUNT_CACHE_NAME_VALUE_DEFAULT);
        setCacheName(cacheName);

    }
}
