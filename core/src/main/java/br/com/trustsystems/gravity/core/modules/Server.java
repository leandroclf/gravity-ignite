package br.com.trustsystems.gravity.core.modules;

import br.com.trustsystems.gravity.core.modules.base.IOTBaseHandler;
import br.com.trustsystems.gravity.core.worker.state.messages.DisconnectMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.Protocol;
import br.com.trustsystems.gravity.system.BaseSystemHandler;

import java.io.Serializable;

public abstract class Server<T> extends IOTBaseHandler {


    /**
     * Declaration by the server implementation if its connections are persistant
     * Or not.
     * Persistent connections are expected to store some control data within the server
     * to ensure successive requests are identifiable.
     *
     * @return
     */
    public abstract boolean isPersistentConnection();

    /**
     * Implementation to return the protocal for this particular implementation
     * Mainly the supported protocals are mqtt and http
     *
     * @return
     */

    public abstract Protocol getProtocal();


    protected abstract IOTMessage toIOTMessage(T serverMessage);


    protected abstract T toServerMessage(IOTMessage internalMessage);


    /**
     * Implementation expected to be called whenever a message is being pushed to
     * workers. This method populates some miscellaneous iotMessage details that are
     * server specific. These data will aid during the process of identification of
     * the return path to the connected device.
     *
     * @param connectionId
     * @param sessionId
     * @param message
     */
    public final void pushToWorker(Serializable connectionId, Serializable sessionId, T message) {

        IOTMessage ioTMessage = toIOTMessage(message);

        if (null == message) {
            dirtyDisconnect(connectionId, sessionId);
            return;
        }

        internalPushToWorker(connectionId, sessionId, ioTMessage);

    }


    private void internalPushToWorker(Serializable connectionId, Serializable sessionId, IOTMessage ioTMessage) {

        ioTMessage.setConnectionId(connectionId);

        if (isPersistentConnection()) {
            //Client specific variables.
            ioTMessage.setSessionId(sessionId);

        }
        //Hardware specific variables
        ioTMessage.setNodeId(getNodeId());
        ioTMessage.setCluster(getCluster());
        ioTMessage.setProtocal(getProtocal());

        getSubscriberList().forEach(subscriber -> subscriber.onNext(ioTMessage));

    }


    public void dirtyDisconnect(Serializable connectionId, Serializable sessionId) {

        DisconnectMessage disconnectMessage = DisconnectMessage.from(true);

        internalPushToWorker(connectionId, sessionId, disconnectMessage);

    }


    @Override
    public int compareTo(BaseSystemHandler baseSystemHandler) {

        if (null == baseSystemHandler)
            throw new NullPointerException("You can't compare a null object.");

        if (baseSystemHandler instanceof Server)
            return 0;
        else
            return -1;
    }


}
