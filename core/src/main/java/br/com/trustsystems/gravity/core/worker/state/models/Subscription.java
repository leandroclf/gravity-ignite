package br.com.trustsystems.gravity.core.worker.state.models;

import br.com.trustsystems.gravity.data.IdKeyComposer;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

public class Subscription implements IdKeyComposer, Serializable {

    @QuerySqlField(orderedGroups = {
            @QuerySqlField.Group(name = "partition_topicfilterkey_qos_idx", order = 0),
            @QuerySqlField.Group(name = "partition_clientid_idx", order = 0)
    })
    private String partition;

    @QuerySqlField(orderedGroups = {
            @QuerySqlField.Group(name = "partition_topicfilterkey_qos_idx", order = 2)
    })
    private long topicFilterKey;

    @QuerySqlField(orderedGroups = {@QuerySqlField.Group(
            name = "partition_topicfilterkey_qos_idx", order = 5)})
    private int qos;


    @QuerySqlField(orderedGroups = {
            @QuerySqlField.Group(name = "partition_clientid_idx", order = 3)
    })
    private String clientId;


    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public long getTopicFilterKey() {
        return topicFilterKey;
    }

    public void setTopicFilterKey(long topicFilterKey) {
        this.topicFilterKey = topicFilterKey;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Serializable generateIdKey() {

        return String.format("%s:%s-%d", getPartition(), getClientId(), getTopicFilterKey());
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "[ partition=" + getPartition() + ", clientId=" + getClientId() + ", topicFilterKey=" + getTopicFilterKey() + ", qos=" + getQos() + "]";
    }
}
