package org.nodex.android.privategroup.invitation;
import org.nodex.android.activity.ActivityScope;
import dagger.Module;
import dagger.Provides;
@Module
public class GroupInvitationModule {
	@ActivityScope
	@Provides
	GroupInvitationController provideInvitationGroupController(
			GroupInvitationControllerImpl groupInvitationController) {
		return groupInvitationController;
	}
}