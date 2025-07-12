package org.nodex.identity;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.db.DatabaseComponent;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.identity.AuthorId;
import org.nodex.core.api.identity.IdentityManager;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.avatar.AvatarManager;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorManager;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import static org.nodex.api.identity.AuthorInfo.Status.OURSELVES;
import static org.nodex.api.identity.AuthorInfo.Status.UNKNOWN;
import static org.nodex.api.identity.AuthorInfo.Status.UNVERIFIED;
import static org.nodex.api.identity.AuthorInfo.Status.VERIFIED;
@ThreadSafe
@NotNullByDefault
class AuthorManagerImpl implements AuthorManager {
	private final DatabaseComponent db;
	private final IdentityManager identityManager;
	private final AvatarManager avatarManager;
	@Inject
	AuthorManagerImpl(DatabaseComponent db, IdentityManager identityManager,
			AvatarManager avatarManager) {
		this.db = db;
		this.identityManager = identityManager;
		this.avatarManager = avatarManager;
	}
	@Override
	public AuthorInfo getAuthorInfo(AuthorId a) throws DbException {
		return db.transactionWithResult(true, txn -> getAuthorInfo(txn, a));
	}
	@Override
	public AuthorInfo getAuthorInfo(Transaction txn, AuthorId authorId)
			throws DbException {
		LocalAuthor localAuthor = identityManager.getLocalAuthor(txn);
		if (localAuthor.getId().equals(authorId)) return getMyAuthorInfo(txn);
		Collection<Contact> contacts = db.getContactsByAuthorId(txn, authorId);
		if (contacts.isEmpty()) return new AuthorInfo(UNKNOWN);
		if (contacts.size() > 1) throw new AssertionError();
		Contact c = contacts.iterator().next();
		return getAuthorInfo(txn, c);
	}
	@Override
	public AuthorInfo getAuthorInfo(Contact c) throws DbException {
		return db.transactionWithResult(true, txn -> getAuthorInfo(txn, c));
	}
	@Override
	public AuthorInfo getAuthorInfo(Transaction txn, Contact c)
			throws DbException {
		AttachmentHeader avatar = avatarManager.getAvatarHeader(txn, c);
		if (c.isVerified())
			return new AuthorInfo(VERIFIED, c.getAlias(), avatar);
		else return new AuthorInfo(UNVERIFIED, c.getAlias(), avatar);
	}
	@Override
	public AuthorInfo getMyAuthorInfo() throws DbException {
		return db.transactionWithResult(true, this::getMyAuthorInfo);
	}
	@Override
	public AuthorInfo getMyAuthorInfo(Transaction txn) throws DbException {
		AttachmentHeader avatar = avatarManager.getMyAvatarHeader(txn);
		return new AuthorInfo(OURSELVES, null, avatar);
	}
}