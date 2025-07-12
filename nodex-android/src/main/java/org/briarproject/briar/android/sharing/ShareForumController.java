package org.nodex.android.sharing;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.contactselection.ContactSelectorController;
import org.nodex.android.contactselection.SelectableContactItem;
import org.nodex.android.controller.handler.ExceptionHandler;
import java.util.Collection;
import javax.annotation.Nullable;
public interface ShareForumController
		extends ContactSelectorController<SelectableContactItem> {
	void share(GroupId g, Collection<ContactId> contacts, @Nullable String text,
			ExceptionHandler<DbException> handler);
}