package br.com.trustsystems.gravity.datastore.ignitecache.internal.impl;

import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.datastore.ignitecache.internal.AbstractHandler;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.commons.configuration.Configuration;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.configuration.CacheConfiguration;
import rx.Observable;

public class MessageHandler extends AbstractHandler<PublishMessage> {

    public static final String CONFIG_IGNITECACHE_MESSAGE_CACHE_NAME = "config.ignitecache.message.cache.name";
    public static final String CONFIG_IGNITECACHE_MESSAGE_CACHE_NAME_VALUE_DEFAULT = "gravity_message_cache";


    @Override
    public void configure(Configuration configuration) {

        String cacheName = configuration.getString(CONFIG_IGNITECACHE_MESSAGE_CACHE_NAME, CONFIG_IGNITECACHE_MESSAGE_CACHE_NAME_VALUE_DEFAULT);
        setCacheName(cacheName);

    }

    @Override
    protected CacheConfiguration setIndexData(Class<PublishMessage> t, CacheConfiguration clCfg) {
        return clCfg.setIndexedTypes(Long.class, t);
    }

    @Override
    public void initializeSequence(String nameOfSequence, Ignite ignite) {

        String queryForCount = "SELECT MAX(id) FROM PublishMessage";
        Object[] params = { };

        Long currentMax = getByQueryAsValue(Long.class, queryForCount, params).toBlocking().single();
        if(null == currentMax){
            currentMax = 0l;
        }

        IgniteAtomicSequence idSequence = ignite.atomicSequence(nameOfSequence, currentMax, true);
        setIdSequence(idSequence);
    }

    public Observable<Long> saveWithIdCheck(PublishMessage publishMessage) {

        try {

            if(publishMessage.getId() <= 0){
                publishMessage.setId(nextId());
            }

            getDatastoreCache().put(publishMessage.generateIdKey(), publishMessage);

            if(PublishMessage.ID_TO_FORCE_GENERATION_ON_SAVE == publishMessage.getMessageId()){



                    long messageId = getPartitionClientMessageId(
                            publishMessage.getPartition(),
                            publishMessage.getClientId(),
                            publishMessage.isInBound(),
                            publishMessage.getId()
                    );

                //Implement max check.

                    publishMessage.setMessageId(messageId);

                getDatastoreCache().put(publishMessage.generateIdKey(), publishMessage);


            }

        } catch (UnRetriableException e) {
            log.error(" save : issues while saving item ", e);
        }

        return Observable.create(observer -> {
            // callback with value
            observer.onNext(publishMessage.getMessageId());
            observer.onCompleted();


        });

    }

    private long getPartitionClientMessageId(String partition, String clientId, boolean isInBound, long id){

        String queryForCount = "SELECT " +
                "(SELECT MAX(messageId) FROM PublishMessage WHERE partition = ? AND clientId = ? AND messageId > ? AND inBound = ? AND id < ? ) " +
                "+ (SELECT COUNT(id) FROM PublishMessage WHERE partition = ? AND clientId = ? AND messageId <= ? AND inBound = ? AND id <= ? ) " +
                " AS newId";
        Object[] params = { partition, clientId, 0, isInBound, id, partition, clientId, 0, isInBound, id };

        Long currentMax = getByQueryAsValue(Long.class, queryForCount, params).toBlocking().single();
        if(null == currentMax){
            currentMax = 1l;
        }

        return currentMax;
    }

}
