package org.nodex.android.sharing;
import org.nodex.core.api.db.DbException;
import org.nodex.android.controller.ActivityLifecycleController;
import org.nodex.android.controller.handler.ExceptionHandler;
import org.nodex.android.controller.handler.ResultExceptionHandler;
import org.nodex.api.sharing.InvitationItem;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
@NotNullByDefault
public interface InvitationController<I extends InvitationItem>
		extends ActivityLifecycleController {
	void loadInvitations(boolean clear,
			ResultExceptionHandler<Collection<I>, DbException> handler);
	void respondToInvitation(I item, boolean accept,
			ExceptionHandler<DbException> handler);
	interface InvitationListener {
		void loadInvitations(boolean clear);
	}
}