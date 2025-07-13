package org.nodex.api.account;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.lifecycle.Service;

/**
 * Manages user account operations.
 */
@NotNullByDefault
public interface AccountManager extends Service {
    
    /**
     * Check if an account exists.
     */
    boolean accountExists();
    
    /**
     * Create a new account.
     */
    void createAccount(String password);
    
    /**
     * Sign in to an existing account.
     */
    boolean signIn(String password);
    
    /**
     * Delete the current account.
     */
    void deleteAccount();
}
