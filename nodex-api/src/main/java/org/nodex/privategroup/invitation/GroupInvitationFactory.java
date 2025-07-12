package org.nodex.api.privategroup.invitation;
import org.nodex.api.contact.Contact;
import org.nodex.api.crypto.CryptoExecutor;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.data.BdfList;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.sync.GroupId;
import org.nodex.nullsafety.NotNullByDefault;
import static org.nodex.api.privategroup.invitation.GroupInvitationManager.CLIENT_ID;
@NotNullByDefault
public interface GroupInvitationFactory {
	String SIGNING_LABEL_INVITE = CLIENT_ID.getString() + "/INVITE";
	@CryptoExecutor
	byte[] signInvitation(Contact c, GroupId privateGroupId, long timestamp,
			PrivateKey privateKey);
	BdfList createInviteToken(AuthorId creatorId, AuthorId memberId,
			GroupId privateGroupId, long timestamp);
}