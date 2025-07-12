package org.nodex.api.sync;

import org.nodex.api.Nameable;
import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Base class for named groups
 */
@NotNullByDefault
public abstract class NamedGroup extends BaseGroup implements Nameable {
    private final String name;

    protected NamedGroup(Group group, String name) {
        super(group);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
