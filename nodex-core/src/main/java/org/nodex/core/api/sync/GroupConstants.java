package org.nodex.core.api.sync;

import org.nodex.api.sync.Group;

/**
 * Constants for sync groups.
 */
public class GroupConstants {
    
    /**
     * Re-export of Group.Visibility from the API.
     */
    public static final class Visibility {
        public static final Group.Visibility PRIVATE = Group.Visibility.PRIVATE;
        public static final Group.Visibility SHARED = Group.Visibility.SHARED;
        public static final Group.Visibility PUBLIC = Group.Visibility.PUBLIC;
    }
}
