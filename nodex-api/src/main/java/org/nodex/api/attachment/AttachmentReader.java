package org.nodex.api.attachment;
import org.nodex.api.db.DbException;
import org.nodex.api.db.NoSuchMessageException;
import org.nodex.api.db.Transaction;
public interface AttachmentReader {
	Attachment getAttachment(AttachmentHeader h) throws DbException;
	Attachment getAttachment(Transaction txn, AttachmentHeader h)
			throws DbException;
}