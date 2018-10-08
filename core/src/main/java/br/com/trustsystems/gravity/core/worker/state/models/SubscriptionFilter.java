package br.com.trustsystems.gravity.core.worker.state.models;

import br.com.trustsystems.gravity.data.IdKeyComposer;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

public class SubscriptionFilter implements Serializable, IdKeyComposer {

    @QuerySqlField(orderedGroups = {
            @QuerySqlField.Group(name = "partition_parentid_name_idx", order = 0)
    })
    private String partition;

    @QuerySqlField(index = true)
    private long id;

    @QuerySqlField(orderedGroups = {
            @QuerySqlField.Group(name = "partition_parentid_name_idx", order = 2)
    })
    private long parentId;

    @QuerySqlField(orderedGroups = {
            @QuerySqlField.Group(name = "partition_parentid_name_idx", order = 3)
    })
    private String name;

    private String fullTreeName;


    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullTreeName() {
        return fullTreeName;
    }

    public void setFullTreeName(String fullTreeName) {
        this.fullTreeName = fullTreeName;
    }

    @Override
    public Serializable generateIdKey() throws UnRetriableException {

        if (0 == getId()) {
            throw new UnRetriableException(" id has to be set before you use the subscription filter");
        }

        return getId();
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + " [ " + " partition = " + getPartition() + "," + " parent = " + getParentId() + "," + " FullTree = " + getFullTreeName() + "," + " ]";
    }
}
