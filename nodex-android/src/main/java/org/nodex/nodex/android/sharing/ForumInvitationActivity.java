package org.nodex.android.sharing;
import android.content.Context;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.api.sharing.SharingInvitationItem;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import static org.nodex.android.sharing.InvitationAdapter.InvitationClickListener;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ForumInvitationActivity
		extends InvitationActivity<SharingInvitationItem> {
	@Inject
	ForumInvitationController controller;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	protected InvitationController<SharingInvitationItem> getController() {
		return controller;
	}
	@Override
	protected InvitationAdapter<SharingInvitationItem, ?> getAdapter(
			Context ctx,
			InvitationClickListener<SharingInvitationItem> listener) {
		return new SharingInvitationAdapter(ctx, listener);
	}
	@Override
	protected int getAcceptRes() {
		return R.string.forum_joined_toast;
	}
	@Override
	protected int getDeclineRes() {
		return R.string.forum_declined_toast;
	}
}