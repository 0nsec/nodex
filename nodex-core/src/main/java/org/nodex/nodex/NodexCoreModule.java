package org.nodex;
import org.nodex.attachment.AttachmentModule;
import org.nodex.autodelete.AutoDeleteModule;
import org.nodex.avatar.AvatarModule;
import org.nodex.blog.BlogModule;
import org.nodex.client.NodexClientModule;
import org.nodex.conversation.ConversationModule;
import org.nodex.feed.FeedModule;
import org.nodex.forum.ForumModule;
import org.nodex.identity.IdentityModule;
import org.nodex.introduction.IntroductionModule;
import org.nodex.messaging.MessagingModule;
import org.nodex.privategroup.PrivateGroupModule;
import org.nodex.privategroup.invitation.GroupInvitationModule;
import org.nodex.sharing.SharingModule;
import org.nodex.test.TestModule;
import dagger.Module;
@Module(includes = {
		AttachmentModule.class,
		AutoDeleteModule.class,
		AvatarModule.class,
		BlogModule.class,
		NodexClientModule.class,
		ConversationModule.class,
		FeedModule.class,
		ForumModule.class,
		GroupInvitationModule.class,
		IdentityModule.class,
		IntroductionModule.class,
		MessagingModule.class,
		PrivateGroupModule.class,
		SharingModule.class,
		TestModule.class
})
public class NodexCoreModule {
}