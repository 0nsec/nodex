package org.nodex.api.attachment;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.NoSuchMessageException;
import org.nodex.core.api.db.Transaction;
public interface AttachmentReader {
	Attachment getAttachment(AttachmentHeader h) throws DbException;
	Attachment getAttachment(Transaction txn, AttachmentHeader h)
			throws DbException;
}