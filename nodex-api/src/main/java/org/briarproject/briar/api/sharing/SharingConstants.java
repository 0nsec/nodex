package org.nodex.api.sharing;
import static org.nodex.core.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
public interface SharingConstants {
	int MAX_INVITATION_TEXT_LENGTH = MAX_MESSAGE_BODY_LENGTH - 1024;
}