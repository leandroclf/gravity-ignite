
package br.com.trustsystems.gravity.core.modules;

import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.core.worker.state.messages.base.Protocol;
import br.com.trustsystems.gravity.system.BaseSystemHandler;
import rx.Subscriber;

public abstract class Eventer extends Subscriber<IOTMessage> implements BaseSystemHandler {

    private Protocol protocal;

    public Protocol getProtocal() {
        return protocal;
    }

    public void setProtocal(Protocol protocal) {
        this.protocal = protocal;
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onCompleted() {

    }

    @Override
    public int compareTo(BaseSystemHandler baseSystemHandler) {

        if(null == baseSystemHandler)
            throw new NullPointerException("You can't compare a null object.");

        if(baseSystemHandler instanceof Eventer)
            return 0;
        else
            return 1;
    }
}
