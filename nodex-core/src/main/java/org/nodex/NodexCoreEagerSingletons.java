package org.nodex;
import org.nodex.autodelete.AutoDeleteModule;
import org.nodex.avatar.AvatarModule;
import org.nodex.conversation.ConversationModule;
import org.nodex.feed.FeedModule;
import org.nodex.forum.ForumModule;
import org.nodex.identity.IdentityModule;
import org.nodex.introduction.IntroductionModule;
import org.nodex.messaging.MessagingModule;
import org.nodex.privategroup.PrivateGroupModule;
import org.nodex.privategroup.invitation.GroupInvitationModule;
import org.nodex.sharing.SharingModule;
public interface NodexCoreEagerSingletons {
	void inject(AutoDeleteModule.EagerSingletons init);
	void inject(AvatarModule.EagerSingletons init);
	void inject(ConversationModule.EagerSingletons init);
	void inject(FeedModule.EagerSingletons init);
	void inject(ForumModule.EagerSingletons init);
	void inject(GroupInvitationModule.EagerSingletons init);
	void inject(IdentityModule.EagerSingletons init);
	void inject(IntroductionModule.EagerSingletons init);
	void inject(MessagingModule.EagerSingletons init);
	void inject(PrivateGroupModule.EagerSingletons init);
	void inject(SharingModule.EagerSingletons init);
	class Helper {
		public static void injectEagerSingletons(NodexCoreEagerSingletons c) {
			c.inject(new AutoDeleteModule.EagerSingletons());
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new ConversationModule.EagerSingletons());
			c.inject(new FeedModule.EagerSingletons());
			c.inject(new ForumModule.EagerSingletons());
			c.inject(new GroupInvitationModule.EagerSingletons());
			c.inject(new MessagingModule.EagerSingletons());
			c.inject(new PrivateGroupModule.EagerSingletons());
			c.inject(new SharingModule.EagerSingletons());
			c.inject(new IdentityModule.EagerSingletons());
			c.inject(new IntroductionModule.EagerSingletons());
		}
	}
}
