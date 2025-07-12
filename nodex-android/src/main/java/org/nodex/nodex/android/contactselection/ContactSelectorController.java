package org.nodex.android.contactselection;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.controller.DbController;
import org.nodex.android.controller.handler.ResultExceptionHandler;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
@NotNullByDefault
public interface ContactSelectorController<I extends BaseSelectableContactItem>
		extends DbController {
	void loadContacts(GroupId g, Collection<ContactId> selection,
			ResultExceptionHandler<Collection<I>, DbException> handler);
}