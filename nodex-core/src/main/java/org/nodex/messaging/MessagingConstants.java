package org.nodex.messaging;
import org.nodex.api.sync.ClientId;
import static java.util.concurrent.TimeUnit.DAYS;
interface MessagingConstants {
	ClientId CLIENT_ID = new ClientId("org.nodex.messaging");
	int MAJOR_VERSION = 1;
	String MSG_KEY_TIMESTAMP = "timestamp";
	String MSG_KEY_LOCAL = "local";
	String MSG_KEY_MSG_TYPE = "messageType";
	String MSG_KEY_HAS_TEXT = "hasText";
	String MSG_KEY_ATTACHMENT_HEADERS = "attachmentHeaders";
	String MSG_KEY_AUTO_DELETE_TIMER = "autoDeleteTimer";
	long MISSING_ATTACHMENT_CLEANUP_DURATION_MS = DAYS.toMillis(28);
}