package br.com.trustsystems.gravity.runner;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;

public interface Runner {


    /**
     * Initializes this instance.
     * <p>
     * This method should be called once the JVM process is created and the
     * <code>Runner</code> instance is created thru its empty public
     * constructor.
     * </p>
     * <p>
     * Apart from set up and allocation of native resources, this method
     * does not start actual operation of <code>Runner</code> (such
     * as starting threads.) as it would impose serious security hazards. The
     * start of operation must be performed in the <code>start()</code>
     * method.
     * </p>
     *
     * @throws UnRetriableException Any exception preventing a successful
     *                              initialization.
     */
    void init() throws UnRetriableException;

    /**
     * Starts the operations of our instance. This
     * method is to be invoked by the environment after the init()
     * method has been successfully invoked and possibly the security
     * level of the JVM has been dropped. Implementors of this
     * method are free to start any number of threads, but need to
     * return control after having done that to enable invocation of
     * the stop()-method.
     */
    void start() throws UnRetriableException;

    /**
     * Stops the operation of this instance and immediately
     * frees any resources allocated by this daemon such as file
     * descriptors or sockets. This method gets called by the container
     * after stop() has been called, before the JVM exits. The Daemon
     * can not be restarted after this method has been called without a
     * new call to the init() method.
     */
    void terminate();

}
