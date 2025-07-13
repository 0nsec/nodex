package org.nodex.api.db;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface CommitAction {
    void run();
}
