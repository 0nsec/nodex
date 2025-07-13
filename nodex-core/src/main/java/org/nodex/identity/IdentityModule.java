package org.nodex.identity;
import org.nodex.api.identity.AuthorManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
@Module
public class IdentityModule {
	public static class EagerSingletons {
		@Inject
		AuthorManager authorManager;
	}
	@Provides
	@Singleton
	AuthorManager provideAuthorManager(AuthorManagerImpl authorManager) {
		return authorManager;
	}
}
