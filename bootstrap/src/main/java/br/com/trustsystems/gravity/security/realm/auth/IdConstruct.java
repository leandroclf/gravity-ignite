package br.com.trustsystems.gravity.security.realm.auth;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Objects;

public class IdConstruct implements Serializable {


    /**
     * The username
     */
    private final String username;

    /**
     * The partition
     */
    private final String partition;

    /**
     * The clientId
     */
    private final String clientId;

    public IdConstruct(String partition, String username, String clientId) {
        this.partition = partition;
        this.username = username;
        this.clientId = clientId;
    }


    public String getUsername() {
        return username;
    }

    public String getPartition() {
        return partition;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSessionId(){
        return String.format("%s-%s-%s",getPartition(), getUsername(), getClientId());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof IdConstruct) {
            IdConstruct sa = (IdConstruct) o;

            if (StringUtils.isEmpty(getClientId()) || StringUtils.isEmpty(sa.getClientId())) {

                return (Objects.equals(sa.getPartition(), getPartition())
                        && Objects.equals(sa.getUsername(), getUsername()));
            } else
                return (
                        Objects.equals(sa.getClientId(), getClientId())
                                && Objects.equals(sa.getPartition(), getPartition())
                                && Objects.equals(sa.getUsername(), getUsername()));

        }
        return false;
    }


}
