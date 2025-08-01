package org.nodex.android.privategroup.memberlist;
import org.nodex.android.activity.ActivityScope;
import dagger.Module;
import dagger.Provides;
@Module
public class GroupMemberModule {
	@ActivityScope
	@Provides
	GroupMemberListController provideGroupMemberListController(
			GroupMemberListControllerImpl groupMemberListController) {
		return groupMemberListController;
	}
}