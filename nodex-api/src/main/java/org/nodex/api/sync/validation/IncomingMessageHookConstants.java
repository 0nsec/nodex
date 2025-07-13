package org.nodex.api.sync.validation;

public class IncomingMessageHookConstants {
    
    public enum DeliveryAction {
        ACCEPT_DO_NOT_SHARE,
        ACCEPT_SHARE,
        REJECT
    }
    
    public static final DeliveryAction ACCEPT_DO_NOT_SHARE = DeliveryAction.ACCEPT_DO_NOT_SHARE;
    public static final DeliveryAction ACCEPT_SHARE = DeliveryAction.ACCEPT_SHARE;
    public static final DeliveryAction REJECT = DeliveryAction.REJECT;
}
