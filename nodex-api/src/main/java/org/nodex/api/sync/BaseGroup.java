package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Base class for groups
 */
@NotNullByDefault
public abstract class BaseGroup {
    private final Group group;

    protected BaseGroup(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

    public GroupId getId() {
        return group.getId();
    }
}
