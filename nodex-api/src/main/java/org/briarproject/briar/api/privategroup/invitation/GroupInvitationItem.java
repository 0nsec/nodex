package org.nodex.api.privategroup.invitation;
import org.nodex.core.api.contact.Contact;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.sharing.InvitationItem;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class GroupInvitationItem extends InvitationItem<PrivateGroup> {
	private final Contact creator;
	public GroupInvitationItem(PrivateGroup privateGroup, Contact creator) {
		super(privateGroup, false);
		this.creator = creator;
	}
	public Contact getCreator() {
		return creator;
	}
}