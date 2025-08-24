package org.nodex;
import org.nodex.autodelete.AutoDeleteModule;
import org.nodex.avatar.AvatarModule;
import org.nodex.conversation.ConversationModule;
import org.nodex.feed.FeedModule;
import org.nodex.identity.IdentityModule;
public interface NodexCoreEagerSingletons {
	void inject(AutoDeleteModule.EagerSingletons init);
	void inject(AvatarModule.EagerSingletons init);
	void inject(ConversationModule.EagerSingletons init);
	void inject(FeedModule.EagerSingletons init);
	// Excluded: ForumModule, GroupInvitationModule
	void inject(IdentityModule.EagerSingletons init);
	// Excluded: PrivateGroupModule, SharingModule
	class Helper {
		public static void injectEagerSingletons(NodexCoreEagerSingletons c) {
			c.inject(new AutoDeleteModule.EagerSingletons());
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new ConversationModule.EagerSingletons());
			c.inject(new FeedModule.EagerSingletons());
			// Excluded: forum and group invitation eager singletons
			// Excluded: messaging, private group and sharing eager singletons
			c.inject(new IdentityModule.EagerSingletons());
		}
	}
}
