package org.nodex.android.settings;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
class OwnIdentityInfo {
	private final LocalAuthor localAuthor;
	private final AuthorInfo authorInfo;
	OwnIdentityInfo(LocalAuthor localAuthor, AuthorInfo authorInfo) {
		this.localAuthor = localAuthor;
		this.authorInfo = authorInfo;
	}
	LocalAuthor getLocalAuthor() {
		return localAuthor;
	}
	AuthorInfo getAuthorInfo() {
		return authorInfo;
	}
}