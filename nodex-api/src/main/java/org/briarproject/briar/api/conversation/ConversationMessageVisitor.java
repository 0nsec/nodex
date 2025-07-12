package org.nodex.api.conversation;
import org.nodex.api.blog.BlogInvitationRequest;
import org.nodex.api.blog.BlogInvitationResponse;
import org.nodex.api.forum.ForumInvitationRequest;
import org.nodex.api.forum.ForumInvitationResponse;
import org.nodex.api.introduction.IntroductionRequest;
import org.nodex.api.introduction.IntroductionResponse;
import org.nodex.api.messaging.PrivateMessageHeader;
import org.nodex.api.privategroup.invitation.GroupInvitationRequest;
import org.nodex.api.privategroup.invitation.GroupInvitationResponse;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface ConversationMessageVisitor<T> {
	T visitPrivateMessageHeader(PrivateMessageHeader h);
	T visitBlogInvitationRequest(BlogInvitationRequest r);
	T visitBlogInvitationResponse(BlogInvitationResponse r);
	T visitForumInvitationRequest(ForumInvitationRequest r);
	T visitForumInvitationResponse(ForumInvitationResponse r);
	T visitGroupInvitationRequest(GroupInvitationRequest r);
	T visitGroupInvitationResponse(GroupInvitationResponse r);
	T visitIntroductionRequest(IntroductionRequest r);
	T visitIntroductionResponse(IntroductionResponse r);
}