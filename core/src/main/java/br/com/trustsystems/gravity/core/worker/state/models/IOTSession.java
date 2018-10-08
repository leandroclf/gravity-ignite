package br.com.trustsystems.gravity.core.worker.state.models;

import br.com.trustsystems.gravity.core.worker.state.session.SerializableUtils;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.shiro.session.Session;

import java.io.Serializable;

public class IOTSession implements Serializable {


    @QuerySqlField(index = true)
    private long expiryTimestamp;

    private String sessionString;

    public IOTSession() {
    }

    public IOTSession(Session session) {


        long expiryTime = session.getLastAccessTime().getTime() + session.getTimeout();
        setExpiryTimestamp(expiryTime);

        setSessionString(SerializableUtils.serialize(session));
    }


    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public void setExpiryTimestamp(long expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }

    public String getSessionString() {
        return sessionString;
    }

    public void setSessionString(String sessionString) {
        this.sessionString = sessionString;
    }

    public Session toSession() {
        return SerializableUtils.deserialize(getSessionString());
    }
}
