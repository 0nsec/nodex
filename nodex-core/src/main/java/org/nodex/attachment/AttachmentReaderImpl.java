package org.nodex.attachment;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.db.DbException;
import org.nodex.api.db.NoSuchMessageException;
import org.nodex.api.db.Transaction;
import org.nodex.api.db.TransactionManager;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.attachment.Attachment;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.attachment.AttachmentReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.inject.Inject;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_DESCRIPTOR_LENGTH;
public class AttachmentReaderImpl implements AttachmentReader {
	private final TransactionManager db;
	private final ClientHelper clientHelper;
	@Inject
	public AttachmentReaderImpl(TransactionManager db,
			ClientHelper clientHelper) {
		this.db = db;
		this.clientHelper = clientHelper;
	}
	@Override
	public Attachment getAttachment(AttachmentHeader h) throws DbException {
		return db.transactionWithResult(true, txn -> getAttachment(txn, h));
	}
	@Override
	public Attachment getAttachment(Transaction txn, AttachmentHeader h)
			throws DbException {
		MessageId m = h.getMessageId();
		Message message = clientHelper.getMessage(txn, m);
		if (!message.getGroupId().equals(h.getGroupId())) {
			throw new NoSuchMessageException();
		}
		byte[] body = message.getBody();
		try {
			BdfDictionary meta =
					clientHelper.getMessageMetadataAsDictionary(txn, m);
			String contentType = meta.getString(MSG_KEY_CONTENT_TYPE);
			if (!contentType.equals(h.getContentType()))
				throw new NoSuchMessageException();
			int offset = meta.getInt(MSG_KEY_DESCRIPTOR_LENGTH);
			InputStream stream = new ByteArrayInputStream(body, offset,
					body.length - offset);
			return new Attachment(h, stream);
		} catch (FormatException e) {
			throw new NoSuchMessageException();
		}
	}
}