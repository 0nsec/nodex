package org.nodex.core.crypto;

import org.nodex.api.crypto.CryptoComponent;
import org.nodex.api.crypto.PasswordStrengthEstimator;
import org.nodex.api.crypto.SecretKey;
import org.nodex.api.crypto.CryptoExecutor;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.api.sync.ValidationManager;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.system.Clock;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.db.DatabaseComponent;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for the crypto system.
 * Provides all cryptographic dependencies.
 */
@Module
public class CryptoModule {

    @Provides
    @Singleton
    CryptoComponent provideCryptoComponent() {
        return new CryptoComponentImpl();
    }

    @Provides
    @Singleton
    PasswordStrengthEstimator providePasswordStrengthEstimator() {
        return new PasswordStrengthEstimatorImpl();
    }

    @Provides
    @Singleton
    CryptoExecutor provideCryptoExecutor() {
        return new CryptoExecutorImpl();
    }
}
