package org.nodex.core.api.sync.validation;

import org.nodex.api.sync.validation.IncomingMessageHook;

/**
 * Constants for sync validation.
 */
public class IncomingMessageHookConstants {
    
    /**
     * Re-export of DeliveryAction from the API.
     */
    public static final class DeliveryAction {
        public static final IncomingMessageHook.DeliveryAction ACCEPT_DO_NOT_SHARE = 
            IncomingMessageHook.DeliveryAction.ACCEPT_DO_NOT_SHARE;
        public static final IncomingMessageHook.DeliveryAction ACCEPT_SHARE = 
            IncomingMessageHook.DeliveryAction.ACCEPT_SHARE;
        public static final IncomingMessageHook.DeliveryAction REJECT = 
            IncomingMessageHook.DeliveryAction.REJECT;
    }
}
