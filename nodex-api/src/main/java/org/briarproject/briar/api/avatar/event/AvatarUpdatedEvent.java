package org.nodex.api.avatar.event;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.event.Event;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class AvatarUpdatedEvent extends Event {
	private final ContactId contactId;
	private final AttachmentHeader attachmentHeader;
	public AvatarUpdatedEvent(ContactId contactId,
			AttachmentHeader attachmentHeader) {
		this.contactId = contactId;
		this.attachmentHeader = attachmentHeader;
	}
	public ContactId getContactId() {
		return contactId;
	}
	public AttachmentHeader getAttachmentHeader() {
		return attachmentHeader;
	}
}