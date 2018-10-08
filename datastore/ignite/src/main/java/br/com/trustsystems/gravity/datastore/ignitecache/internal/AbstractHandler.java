package br.com.trustsystems.gravity.datastore.ignitecache.internal;

import br.com.trustsystems.gravity.core.worker.exceptions.DoesNotExistException;
import br.com.trustsystems.gravity.data.IdKeyComposer;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.commons.configuration.Configuration;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMemoryMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicy;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.cache.Cache.Entry;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public abstract class AbstractHandler<T extends IdKeyComposer> implements Serializable {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private String cacheName;
    private IgniteCache<Serializable, T> datastoreCache;

    private IgniteAtomicSequence idSequence;

    private Class<T> classType;

    public static String preparePlaceHolders(int length) {
        StringBuilder builder = new StringBuilder(length * 2 - 1);
        for (int i = 0; i < length; i++) {
            if (i > 0) builder.append(',');
            builder.append('?');
        }
        return builder.toString();
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public IgniteCache<Serializable, T> getDatastoreCache() {
        return datastoreCache;
    }

    public void setDatastoreCache(IgniteCache<Serializable, T> datastoreCache) {
        this.datastoreCache = datastoreCache;
    }

    public IgniteAtomicSequence getIdSequence() {
        return idSequence;
    }

    public void setIdSequence(IgniteAtomicSequence idSequence) {
        this.idSequence = idSequence;
    }

    public abstract void configure(Configuration configuration);

    public void initiate(Class<T> t, Ignite ignite) {

        CacheConfiguration configuration = new CacheConfiguration();

        configuration.setName(getCacheName());
        configuration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        configuration.setCacheMode(CacheMode.PARTITIONED);

        configuration = setIndexData(t, configuration);
        configuration.setMemoryMode(CacheMemoryMode.ONHEAP_TIERED);

        LruEvictionPolicy lruEvictionPolicy = new LruEvictionPolicy(5170000);
        configuration.setEvictionPolicy(lruEvictionPolicy);

        configuration.setSwapEnabled(true);

        ignite.createCache(configuration);
        IgniteCache clientIgniteCache = ignite.cache(getCacheName()).withAsync();

        setDatastoreCache(clientIgniteCache);


        classType = t;

        String nameOfSequence = getCacheName() + "-sequence";
        initializeSequence(nameOfSequence, ignite);

    }

    public void initializeSequence(String nameOfSequence, Ignite ignite) {

        long currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        IgniteAtomicSequence idSequence = ignite.atomicSequence(nameOfSequence, currentTime, true);
        setIdSequence(idSequence);
    }

    protected CacheConfiguration setIndexData(Class<T> t, CacheConfiguration clCfg) {

        clCfg.setIndexedTypes(String.class, t);
        return clCfg;
    }

    public long nextId() {
        return idSequence.incrementAndGet();
    }

    public Observable<T> getByKey(Serializable key) {

        return Observable.create(observer -> {

            try {
                // do work on separate thread
                getDatastoreCache().get(key);
                IgniteFuture<T> future = getDatastoreCache().future();

                future.listen(value -> {
                    // callback with value only if not null

                    T actualResult = value.get();
                    if (null != actualResult) {
                        observer.onNext(actualResult);
                        observer.onCompleted();
                    } else {
                        observer.onError(new DoesNotExistException(String.format("%s with key [%s] does not exist.", classType, key)));
                    }

                });


            } catch (Exception e) {
                observer.onError(e);
            }

        });

    }

    public Observable<T> getByKeyWithDefault(Serializable key, T defaultValue) {

        return Observable.create(observer -> {

            try {
                // do work on separate thread

                getDatastoreCache().get(key);
                IgniteFuture<T> future = getDatastoreCache().future();

                future.listen(f -> {
                    // callback with value only if not null
                    T value = f.get();
                    if (null != value) {
                        observer.onNext(value);
                    } else {
                        observer.onNext(defaultValue);
                    }
                    observer.onCompleted();


                });
            } catch (Exception e) {
                observer.onError(e);
            }

        });

    }

    public Observable<T> getByQuery(Class<T> t, String query, Object[] params) {

        return Observable.create(observer -> {

            try {

                SqlQuery sql = new SqlQuery<Serializable, T>(t, query);
                sql.setArgs(params);

                // Find all messages belonging to a client.
                QueryCursor<Entry<Serializable, T>> queryResult = getDatastoreCache().query(sql);

                for (Entry<Serializable, T> entry : queryResult) {
                    // callback with value
                    observer.onNext(entry.getValue());
                }


                observer.onCompleted();
            } catch (Exception e) {
                observer.onError(e);
            }

        });

    }

    public <L extends Serializable> Observable<L> getByQueryAsValue(Class<L> l, String query, Object[] params) {

        return Observable.create(observer -> {

            try {

                SqlFieldsQuery sql = new SqlFieldsQuery(query);


                // Execute the query and obtain the query result cursor.
                try (QueryCursor<List<?>> queryResult = getDatastoreCache().query(sql.setArgs(params))) {
                    // callback with value

                    for (List entry : queryResult) {
                        // callback with value
                        observer.onNext((L) entry.get(0));
                    }

                }

                observer.onCompleted();
            } catch (Exception e) {
                observer.onError(e);
            }

        });

    }

    public Observable<List> getByQueryAsValueList(String query, Object[] params) {

        return Observable.create(observer -> {

            try {

                SqlFieldsQuery sql = new SqlFieldsQuery(query);


                // Execute the query and obtain the query result cursor.
                try (QueryCursor<?> cursor = getDatastoreCache().query(sql.setArgs(params))) {
                    // callback with value
                    observer.onNext(cursor.getAll());
                }

                observer.onCompleted();
            } catch (Exception e) {
                observer.onError(e);
            }

        });

    }

    public void save(T item) {
        try {
            getDatastoreCache().put(item.generateIdKey(), item);
        } catch (UnRetriableException e) {
            log.error(" save : issues while saving item ", e);
        }

    }

    public void remove(IdKeyComposer item) {

        try {
            getDatastoreCache().remove(item.generateIdKey());
        } catch (UnRetriableException e) {

        }

    }


}
