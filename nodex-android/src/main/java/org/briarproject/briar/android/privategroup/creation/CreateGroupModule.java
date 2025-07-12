package org.nodex.android.privategroup.creation;
import org.nodex.android.activity.ActivityScope;
import dagger.Module;
import dagger.Provides;
@Module
public class CreateGroupModule {
	@ActivityScope
	@Provides
	CreateGroupController provideCreateGroupController(
			CreateGroupControllerImpl createGroupController) {
		return createGroupController;
	}
}