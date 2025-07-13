package org.nodex.api.system;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.util.concurrent.Executor;

@NotNullByDefault
public interface TaskScheduler extends Executor {
    
    /**
     * Schedule a task to run after a delay.
     */
    void schedule(Runnable task, long delayMs);
    
    /**
     * Schedule a task to run periodically.
     */
    void scheduleRepeating(Runnable task, long initialDelayMs, long periodMs);
    
    /**
     * Cancel all scheduled tasks.
     */
    void cancelAll();
}
