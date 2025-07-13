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
        public static final Group.Visibility INVISIBLE = Group.Visibility.INVISIBLE;
        public static final Group.Visibility VISIBLE = Group.Visibility.VISIBLE;
        public static final Group.Visibility SHARED = Group.Visibility.SHARED;
    }
}
