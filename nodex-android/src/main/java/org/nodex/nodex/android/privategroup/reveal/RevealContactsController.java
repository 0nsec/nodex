package org.nodex.android.privategroup.reveal;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.contactselection.ContactSelectorController;
import org.nodex.android.controller.handler.ExceptionHandler;
import org.nodex.android.controller.handler.ResultExceptionHandler;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
@NotNullByDefault
public interface RevealContactsController
		extends ContactSelectorController<RevealableContactItem> {
	void isOnboardingNeeded(
			ResultExceptionHandler<Boolean, DbException> handler);
	void onboardingShown(ExceptionHandler<DbException> handler);
	void reveal(GroupId g, Collection<ContactId> contacts,
			ExceptionHandler<DbException> handler);
}