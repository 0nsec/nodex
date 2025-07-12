package org.nodex.android.sharing;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.ClientId;
import org.nodex.android.controller.handler.ExceptionHandler;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogSharingManager;
import org.nodex.api.blog.event.BlogInvitationRequestReceivedEvent;
import org.nodex.api.sharing.SharingInvitationItem;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.api.blog.BlogManager.CLIENT_ID;
@NotNullByDefault
class BlogInvitationControllerImpl
		extends InvitationControllerImpl<SharingInvitationItem>
		implements BlogInvitationController {
	private final BlogSharingManager blogSharingManager;
	@Inject
	BlogInvitationControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, EventBus eventBus,
			BlogSharingManager blogSharingManager) {
		super(dbExecutor, lifecycleManager, eventBus);
		this.blogSharingManager = blogSharingManager;
	}
	@Override
	public void eventOccurred(Event e) {
		super.eventOccurred(e);
		if (e instanceof BlogInvitationRequestReceivedEvent) {
			LOG.info("Blog invitation received, reloading");
			listener.loadInvitations(false);
		}
	}
	@Override
	protected ClientId getShareableClientId() {
		return CLIENT_ID;
	}
	@Override
	protected Collection<SharingInvitationItem> getInvitations() throws DbException {
		return blogSharingManager.getInvitations();
	}
	@Override
	public void respondToInvitation(SharingInvitationItem item, boolean accept,
			ExceptionHandler<DbException> handler) {
		runOnDbThread(() -> {
			try {
				Blog f = (Blog) item.getShareable();
				for (Contact c : item.getNewSharers()) {
					blogSharingManager.respondToInvitation(f, c, accept);
				}
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			}
		});
	}
}