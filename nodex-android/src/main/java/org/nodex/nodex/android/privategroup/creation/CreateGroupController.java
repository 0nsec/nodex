package org.nodex.android.privategroup.creation;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.contactselection.ContactSelectorController;
import org.nodex.android.contactselection.SelectableContactItem;
import org.nodex.android.controller.handler.ResultExceptionHandler;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import androidx.annotation.Nullable;
@NotNullByDefault
public interface CreateGroupController
		extends ContactSelectorController<SelectableContactItem> {
	void createGroup(String name,
			ResultExceptionHandler<GroupId, DbException> result);
	void sendInvitation(GroupId g, Collection<ContactId> contacts,
			@Nullable String text,
			ResultExceptionHandler<Void, DbException> result);
}