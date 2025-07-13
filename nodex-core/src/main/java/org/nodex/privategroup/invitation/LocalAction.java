package org.nodex.privategroup.invitation;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class LocalAction {
    
    public enum Type {
        CREATE_INVITATION,
        SEND_INVITATION,
        ACCEPT_INVITATION,
        DECLINE_INVITATION,
        LEAVE_GROUP
    }
    
    private final Type type;
    private final Object data;
    
    public LocalAction(Type type, Object data) {
        this.type = type;
        this.data = data;
    }
    
    public Type getType() {
        return type;
    }
    
    public Object getData() {
        return data;
    }
}
