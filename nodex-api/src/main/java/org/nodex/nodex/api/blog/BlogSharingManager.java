package org.nodex.api.blog;
import org.nodex.core.api.sync.ClientId;
import org.nodex.api.sharing.SharingManager;
public interface BlogSharingManager extends SharingManager<Blog> {
	ClientId CLIENT_ID = new ClientId("org.nodex.blog.sharing");
	int MAJOR_VERSION = 0;
	int MINOR_VERSION = 1;
}