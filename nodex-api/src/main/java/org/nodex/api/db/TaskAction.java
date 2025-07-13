package org.nodex.api.db;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class TaskAction implements CommitAction {
    
    private final Runnable task;
    
    public TaskAction(Runnable task) {
        this.task = task;
    }
    
    @Override
    public void run() {
        task.run();
    }
}
