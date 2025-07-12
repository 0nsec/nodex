package org.nodex.api.forum;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sharing.SharingManager;
public interface ForumSharingManager extends SharingManager<Forum> {
	ClientId CLIENT_ID = new ClientId("org.nodex.forum.sharing");
	int MAJOR_VERSION = 0;
	int MINOR_VERSION = 1;
}