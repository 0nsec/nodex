package org.nodex.android.sharing;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.ClientId;
import org.nodex.android.controller.handler.ExceptionHandler;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.api.forum.event.ForumInvitationRequestReceivedEvent;
import org.nodex.api.sharing.SharingInvitationItem;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.api.forum.ForumManager.CLIENT_ID;
@NotNullByDefault
class ForumInvitationControllerImpl
		extends InvitationControllerImpl<SharingInvitationItem>
		implements ForumInvitationController {
	private final ForumSharingManager forumSharingManager;
	@Inject
	ForumInvitationControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, EventBus eventBus,
			ForumSharingManager forumSharingManager) {
		super(dbExecutor, lifecycleManager, eventBus);
		this.forumSharingManager = forumSharingManager;
	}
	@Override
	public void eventOccurred(Event e) {
		super.eventOccurred(e);
		if (e instanceof ForumInvitationRequestReceivedEvent) {
			LOG.info("Forum invitation received, reloading");
			listener.loadInvitations(false);
		}
	}
	@Override
	protected ClientId getShareableClientId() {
		return CLIENT_ID;
	}
	@Override
	protected Collection<SharingInvitationItem> getInvitations()
			throws DbException {
		return forumSharingManager.getInvitations();
	}
	@Override
	public void respondToInvitation(SharingInvitationItem item, boolean accept,
			ExceptionHandler<DbException> handler) {
		runOnDbThread(() -> {
			try {
				Forum f = (Forum) item.getShareable();
				for (Contact c : item.getNewSharers()) {
					forumSharingManager.respondToInvitation(f, c, accept);
				}
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			}
		});
	}
}