package org.nodex.autodelete;
import org.nodex.api.contact.ContactManager;
import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.api.autodelete.AutoDeleteManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
@Module
public class AutoDeleteModule {
	public static class EagerSingletons {
		@Inject
		AutoDeleteManager autoDeleteManager;
	}
	@Provides
	@Singleton
	AutoDeleteManager provideAutoDeleteManager(
			LifecycleManager lifecycleManager, ContactManager contactManager, AutoDeleteManager autoDeleteManager) {
		lifecycleManager.registerOpenDatabaseHook((LifecycleManager.OpenDatabaseHook) autoDeleteManager);
		contactManager.registerContactHook((ContactManager.ContactHook) autoDeleteManager);
		return autoDeleteManager;
	}
}
