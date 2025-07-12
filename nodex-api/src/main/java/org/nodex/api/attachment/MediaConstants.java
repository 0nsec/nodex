package org.nodex.api.attachment;
import static org.nodex.core.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
public interface MediaConstants {
	String MSG_KEY_CONTENT_TYPE = "contentType";
	String MSG_KEY_DESCRIPTOR_LENGTH = "descriptorLength";
	int MAX_CONTENT_TYPE_BYTES = 80;
	int MAX_IMAGE_SIZE = MAX_MESSAGE_BODY_LENGTH - 100;
}