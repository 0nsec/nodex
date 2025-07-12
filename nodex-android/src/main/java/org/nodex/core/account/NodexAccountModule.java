package org.nodex.core.account;
import org.nodex.core.api.account.AccountManager;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
@Module
public class NodexAccountModule {
	@Provides
	@Singleton
	AccountManager provideAccountManager(NodexAccountManager accountManager) {
		return accountManager;
	}
}