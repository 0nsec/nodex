package org.nodex.api.avatar;
import org.nodex.api.contact.Contact;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.ClientId;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
@NotNullByDefault
public interface AvatarManager {
	ClientId CLIENT_ID = new ClientId("org.nodex.avatar");
	int MAJOR_VERSION = 0;
	int MINOR_VERSION = 0;
	AttachmentHeader addAvatar(String contentType, InputStream in)
			throws DbException, IOException;
	@Nullable
	AttachmentHeader getAvatarHeader(Transaction txn, Contact c)
			throws DbException;
	@Nullable
	AttachmentHeader getMyAvatarHeader(Transaction txn) throws DbException;
}