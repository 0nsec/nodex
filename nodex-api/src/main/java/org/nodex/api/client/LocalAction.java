package org.nodex.api.client;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public abstract class LocalAction {
    
    public enum Type {
        REQUEST,
        ACCEPT,
        DECLINE,
        ABORT
    }
    
    private final Type type;
    
    protected LocalAction(Type type) {
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
    
    public static class RequestAction extends LocalAction {
        private final String text;
        
        public RequestAction(String text) {
            super(Type.REQUEST);
            this.text = text;
        }
        
        public String getText() {
            return text;
        }
    }
    
    public static class AcceptAction extends LocalAction {
        public AcceptAction() {
            super(Type.ACCEPT);
        }
    }
    
    public static class DeclineAction extends LocalAction {
        public DeclineAction() {
            super(Type.DECLINE);
        }
    }
    
    public static class AbortAction extends LocalAction {
        public AbortAction() {
            super(Type.ABORT);
        }
    }
}
