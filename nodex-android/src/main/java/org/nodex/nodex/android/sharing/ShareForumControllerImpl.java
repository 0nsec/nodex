package org.nodex.android.sharing;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.NoSuchContactException;
import org.nodex.core.api.db.NoSuchGroupException;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.contactselection.ContactSelectorControllerImpl;
import org.nodex.android.controller.handler.ExceptionHandler;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.api.identity.AuthorManager;
import org.nodex.api.sharing.SharingManager.SharingStatus;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logException;
@Immutable
@NotNullByDefault
class ShareForumControllerImpl extends ContactSelectorControllerImpl
		implements ShareForumController {
	private final static Logger LOG =
			getLogger(ShareForumControllerImpl.class.getName());
	private final ForumSharingManager forumSharingManager;
	@Inject
	ShareForumControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, ContactManager contactManager,
			AuthorManager authorManager,
			ForumSharingManager forumSharingManager) {
		super(dbExecutor, lifecycleManager, contactManager, authorManager);
		this.forumSharingManager = forumSharingManager;
	}
	@Override
	protected SharingStatus getSharingStatus(GroupId g, Contact c)
			throws DbException {
		return forumSharingManager.getSharingStatus(g, c);
	}
	@Override
	public void share(GroupId g, Collection<ContactId> contacts,
			@Nullable String text, ExceptionHandler<DbException> handler) {
		runOnDbThread(() -> {
			try {
				for (ContactId c : contacts) {
					try {
						forumSharingManager.sendInvitation(g, c, text);
					} catch (NoSuchContactException | NoSuchGroupException e) {
						logException(LOG, WARNING, e);
					}
				}
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			}
		});
	}
}