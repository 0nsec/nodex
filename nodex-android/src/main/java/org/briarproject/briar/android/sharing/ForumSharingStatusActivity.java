package org.nodex.android.sharing;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.event.Event;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.api.forum.ForumInvitationResponse;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.api.forum.event.ForumInvitationResponseReceivedEvent;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.Collection;
import javax.inject.Inject;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ForumSharingStatusActivity extends SharingStatusActivity {
	@Inject
	protected volatile ForumSharingManager forumSharingManager;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	public void eventOccurred(Event e) {
		super.eventOccurred(e);
		if (e instanceof ForumInvitationResponseReceivedEvent) {
			ForumInvitationResponseReceivedEvent r =
					(ForumInvitationResponseReceivedEvent) e;
			ForumInvitationResponse h = r.getMessageHeader();
			if (h.getShareableId().equals(getGroupId()) && h.wasAccepted()) {
				loadSharedWith();
			}
		}
	}
	@Override
	int getInfoText() {
		return R.string.sharing_status_forum;
	}
	@Override
	@DatabaseExecutor
	protected Collection<Contact> getSharedWith() throws DbException {
		return forumSharingManager.getSharedWith(getGroupId());
	}
}