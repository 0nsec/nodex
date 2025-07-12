package org.nodex.android.privategroup.invitation;
import org.nodex.android.sharing.InvitationController;
import org.nodex.api.privategroup.invitation.GroupInvitationItem;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
interface GroupInvitationController
		extends InvitationController<GroupInvitationItem> {
}