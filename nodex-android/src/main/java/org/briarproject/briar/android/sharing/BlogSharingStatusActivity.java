package org.nodex.android.sharing;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.event.Event;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.api.blog.BlogInvitationResponse;
import org.nodex.api.blog.BlogSharingManager;
import org.nodex.api.blog.event.BlogInvitationResponseReceivedEvent;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.Collection;
import javax.inject.Inject;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class BlogSharingStatusActivity extends SharingStatusActivity {
	@Inject
	protected volatile BlogSharingManager blogSharingManager;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	public void eventOccurred(Event e) {
		super.eventOccurred(e);
		if (e instanceof BlogInvitationResponseReceivedEvent) {
			BlogInvitationResponseReceivedEvent r =
					(BlogInvitationResponseReceivedEvent) e;
			BlogInvitationResponse h = r.getMessageHeader();
			if (h.getShareableId().equals(getGroupId()) && h.wasAccepted()) {
				loadSharedWith();
			}
		}
	}
	@Override
	int getInfoText() {
		return R.string.sharing_status_blog;
	}
	@Override
	@DatabaseExecutor
	protected Collection<Contact> getSharedWith() throws DbException {
		return blogSharingManager.getSharedWith(getGroupId());
	}
}