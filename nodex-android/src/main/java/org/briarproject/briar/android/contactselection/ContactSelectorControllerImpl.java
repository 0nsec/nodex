package org.nodex.android.contactselection;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.controller.DbControllerImpl;
import org.nodex.android.controller.handler.ResultExceptionHandler;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorManager;
import org.nodex.api.sharing.SharingManager.SharingStatus;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.util.LogUtils.logException;
@Immutable
@NotNullByDefault
public abstract class ContactSelectorControllerImpl
		extends DbControllerImpl
		implements ContactSelectorController<SelectableContactItem> {
	private static final Logger LOG =
			Logger.getLogger(ContactSelectorControllerImpl.class.getName());
	private final ContactManager contactManager;
	private final AuthorManager authorManager;
	public ContactSelectorControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, ContactManager contactManager,
			AuthorManager authorManager) {
		super(dbExecutor, lifecycleManager);
		this.contactManager = contactManager;
		this.authorManager = authorManager;
	}
	@Override
	public void loadContacts(GroupId g, Collection<ContactId> selection,
			ResultExceptionHandler<Collection<SelectableContactItem>, DbException> handler) {
		runOnDbThread(() -> {
			try {
				Collection<SelectableContactItem> contacts = new ArrayList<>();
				for (Contact c : contactManager.getContacts()) {
					AuthorInfo authorInfo = authorManager.getAuthorInfo(c);
					boolean selected = selection.contains(c.getId());
					contacts.add(new SelectableContactItem(c, authorInfo,
							selected, getSharingStatus(g, c)));
				}
				handler.onResult(contacts);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			}
		});
	}
	@DatabaseExecutor
	protected abstract SharingStatus getSharingStatus(GroupId g, Contact c)
			throws DbException;
}