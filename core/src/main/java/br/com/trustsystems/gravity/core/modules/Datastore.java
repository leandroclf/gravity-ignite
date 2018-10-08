package br.com.trustsystems.gravity.core.modules;

import br.com.trustsystems.gravity.core.worker.state.Constant;
import br.com.trustsystems.gravity.core.worker.state.messages.PublishMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.RetainedMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.WillMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.core.worker.state.models.Client;
import br.com.trustsystems.gravity.core.worker.state.models.Subscription;
import br.com.trustsystems.gravity.core.worker.state.models.SubscriptionFilter;
import br.com.trustsystems.gravity.security.realm.IOTAccountDatastore;
import br.com.trustsystems.gravity.system.BaseSystemHandler;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public abstract class Datastore implements IOTAccountDatastore, Observable.OnSubscribe<IOTMessage>, BaseSystemHandler {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private List<Eventer> eventerList = new ArrayList<>();

    private Ignite ignite;

    private ExecutorService executorService;

    protected Ignite getIgnite() {
        return ignite;
    }

    public void setIgnite(Ignite ignite) {
        this.ignite = ignite;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }


    public abstract Observable<Client> getClient(String partition, String clientIdentifier);


    public abstract void saveClient(Client client);

    public abstract void removeClient(Client client);

    public abstract Observable<WillMessage> getWill(Serializable willKey);

    public abstract void saveWill(WillMessage will);

    public abstract void removeWill(WillMessage will);

    public abstract Observable<SubscriptionFilter> getMatchingSubscriptionFilter(String partition, String topic);

    public abstract Observable<SubscriptionFilter> getSubscriptionFilter(String partition, String topic);

    public abstract Observable<SubscriptionFilter> getOrCreateSubscriptionFilter(String partition, String topic);

    public abstract void removeSubscriptionFilter(SubscriptionFilter subscriptionFilter);


    public abstract Observable<Subscription> getSubscriptions(Client client);

    public abstract Observable<Subscription> getSubscriptions(String partition, long topicFilterKey, int qos);

    public abstract void saveSubscription(Subscription subscription);

    public abstract void removeSubscription(Subscription subscription);


    public abstract Observable<PublishMessage> getMessages(Client client);

    public abstract Observable<PublishMessage> getMessage(String partition, String clientIdentifier, long messageId, boolean isInbound);

    public abstract Observable<Long> saveMessage(PublishMessage publishMessage);

    public abstract void removeMessage(PublishMessage publishMessage);

    public abstract Observable<RetainedMessage> getRetainedMessage(String partition, long topicFilterId);

    public abstract void saveRetainedMessage(RetainedMessage publishMessage);

    public abstract void removeRetainedMessage(RetainedMessage publishMessage);

    public abstract String nextClientId();

    /**
     * getTopicNavigationRoute is a utility method to breakdown route to
     * Subscription filter names...
     *
     * @param topicFilter
     * @return
     */
    public List<String> getTopicNavigationRoute(String topicFilter) {

        List<String> topicBreakDownSet = new LinkedList<>();

        String[] topicLevels = topicFilter.split(Constant.PATH_SEPARATOR);

        Collections.addAll(topicBreakDownSet, topicLevels);

        return topicBreakDownSet;
    }


    @Override
    public void call(Subscriber<? super IOTMessage> subscriber) {

        if (subscriber instanceof Eventer) {
            eventerList.add((Eventer) subscriber);
        }

    }

    @Override
    public int compareTo(BaseSystemHandler baseSystemHandler) {

        if (null == baseSystemHandler)
            throw new NullPointerException("You can't compare a null object.");

        if (baseSystemHandler instanceof Datastore)
            return 0;
        else if (baseSystemHandler instanceof Eventer)
            return -1;

        else
            return 1;
    }


}
