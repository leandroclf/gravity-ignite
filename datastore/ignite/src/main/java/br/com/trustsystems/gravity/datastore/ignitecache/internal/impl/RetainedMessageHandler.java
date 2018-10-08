package br.com.trustsystems.gravity.datastore.ignitecache.internal.impl;

import br.com.trustsystems.gravity.core.worker.state.messages.RetainedMessage;
import br.com.trustsystems.gravity.datastore.ignitecache.internal.AbstractHandler;
import org.apache.commons.configuration.Configuration;

public class RetainedMessageHandler extends AbstractHandler<RetainedMessage> {

    public static final String CONFIG_IGNITECACHE_RETAINED_MESSAGE_CACHE_NAME = "config.ignitecache.retained.message.cache.name";
    public static final String CONFIG_IGNITECACHE_RETAINED_MESSAGE_CACHE_NAME_VALUE_DEFAULT = "gravity_retained_message_cache";


    @Override
    public void configure(Configuration configuration) {

        String cacheName = configuration.getString(CONFIG_IGNITECACHE_RETAINED_MESSAGE_CACHE_NAME, CONFIG_IGNITECACHE_RETAINED_MESSAGE_CACHE_NAME_VALUE_DEFAULT);
        setCacheName(cacheName);

    }

}
