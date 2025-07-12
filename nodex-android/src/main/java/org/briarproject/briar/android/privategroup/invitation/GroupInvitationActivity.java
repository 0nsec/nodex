package org.nodex.android.privategroup.invitation;
import android.content.Context;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.sharing.InvitationActivity;
import org.nodex.android.sharing.InvitationAdapter;
import org.nodex.api.privategroup.invitation.GroupInvitationItem;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import static org.nodex.android.sharing.InvitationAdapter.InvitationClickListener;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class GroupInvitationActivity
		extends InvitationActivity<GroupInvitationItem> {
	@Inject
	protected GroupInvitationController controller;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	protected GroupInvitationController getController() {
		return controller;
	}
	@Override
	protected InvitationAdapter<GroupInvitationItem, ?> getAdapter(Context ctx,
			InvitationClickListener<GroupInvitationItem> listener) {
		return new GroupInvitationAdapter(ctx, listener);
	}
	@Override
	protected int getAcceptRes() {
		return R.string.groups_invitations_joined;
	}
	@Override
	protected int getDeclineRes() {
		return R.string.groups_invitations_declined;
	}
}