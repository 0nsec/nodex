package org.nodex.api.lifecycle;

import java.util.concurrent.Executor;

/**
 * Executor for IO operations.
 */
public interface IoExecutor extends Executor {
    
    /**
     * Executes the given command at some time in the future using an IO thread.
     */
    @Override
    void execute(Runnable command);
    
    /**
     * Shuts down the executor.
     */
    void shutdown();
    
    /**
     * Returns true if the executor has been shut down.
     */
    boolean isShutdown();
}
