package org.nodex.privategroup.invitation;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.client.ContactGroupFactory;
import org.nodex.api.contact.Contact;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.data.BdfList;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupId;
import org.nodex.api.privategroup.invitation.GroupInvitationFactory;
import org.nodex.api.nullsafety.NotNullByDefault;
import java.security.GeneralSecurityException;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.api.privategroup.invitation.GroupInvitationManager.CLIENT_ID;
import static org.nodex.api.privategroup.invitation.GroupInvitationManager.MAJOR_VERSION;
@Immutable
@NotNullByDefault
class GroupInvitationFactoryImpl implements GroupInvitationFactory {
	private final ContactGroupFactory contactGroupFactory;
	private final ClientHelper clientHelper;
	@Inject
	GroupInvitationFactoryImpl(ContactGroupFactory contactGroupFactory,
			ClientHelper clientHelper) {
		this.contactGroupFactory = contactGroupFactory;
		this.clientHelper = clientHelper;
	}
	@Override
	public byte[] signInvitation(Contact c, GroupId privateGroupId,
			long timestamp, PrivateKey privateKey) {
		AuthorId creatorId = c.getLocalAuthorId();
		AuthorId memberId = c.getAuthor().getId();
		BdfList token = createInviteToken(creatorId, memberId, privateGroupId,
				timestamp);
		try {
			return clientHelper.sign(SIGNING_LABEL_INVITE, token, privateKey);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
	@Override
	public BdfList createInviteToken(AuthorId creatorId, AuthorId memberId,
			GroupId privateGroupId, long timestamp) {
		Group contactGroup = contactGroupFactory.createContactGroup(CLIENT_ID,
				MAJOR_VERSION, creatorId, memberId);
		return BdfList.of(
				timestamp,
				contactGroup.getId(),
				privateGroupId
		);
	}
}
