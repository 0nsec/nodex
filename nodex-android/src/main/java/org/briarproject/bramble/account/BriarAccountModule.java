package org.nodex.core.account;
import org.nodex.core.api.account.AccountManager;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
@Module
public class BriarAccountModule {
	@Provides
	@Singleton
	AccountManager provideAccountManager(BriarAccountManager accountManager) {
		return accountManager;
	}
}