package org.nodex.api.privategroup;

import org.nodex.api.identity.Author;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.NamedGroup;
import org.nodex.api.sync.Group;

import javax.annotation.concurrent.Immutable;

/**
 * A private group for sharing messages among members.
 */
@Immutable
@NotNullByDefault
public class PrivateGroup extends NamedGroup {

    private final Author creator;
    private final boolean dissolved;

    public PrivateGroup(Group group, String name, Author creator, boolean dissolved) {
        super(group, name);
        this.creator = creator;
        this.dissolved = dissolved;
    }

    public Author getCreator() {
        return creator;
    }

    public boolean isDissolved() {
        return dissolved;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PrivateGroup && super.equals(o);
    }

    @Override
    public String toString() {
        return "PrivateGroup{name='" + getName() + "', creator=" + creator.getName() + '}';
    }
}
