package org.nodex.android.privategroup.reveal;
import org.nodex.android.activity.ActivityScope;
import dagger.Module;
import dagger.Provides;
@Module
public class GroupRevealModule {
	@ActivityScope
	@Provides
	RevealContactsController provideRevealContactsController(
			RevealContactsControllerImpl revealContactsController) {
		return revealContactsController;
	}
}