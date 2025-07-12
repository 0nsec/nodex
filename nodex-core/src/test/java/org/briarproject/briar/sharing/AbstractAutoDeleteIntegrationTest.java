package org.nodex.sharing;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.sync.MessageId;
import org.nodex.api.autodelete.event.ConversationMessagesDeletedEvent;
import org.nodex.api.blog.Blog;
import org.nodex.api.client.BaseGroup;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.api.forum.Forum;
import org.nodex.api.sharing.InvitationResponse;
import org.nodex.api.sharing.Shareable;
import org.nodex.api.sharing.SharingInvitationItem;
import org.nodex.api.sharing.SharingManager;
import org.nodex.autodelete.AbstractAutoDeleteTest;
import org.junit.Test;
import java.util.Collection;
import static org.nodex.core.api.cleanup.CleanupManager.BATCH_DELAY_MS;
import static org.nodex.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.sharing.SharingManager.SharingStatus.SHAREABLE;
import static org.nodex.test.TestEventListener.assertEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
public abstract class AbstractAutoDeleteIntegrationTest
		extends AbstractAutoDeleteTest {
	protected abstract SharingManager<? extends Shareable> getSharingManager0();
	protected abstract SharingManager<? extends Shareable> getSharingManager1();
	protected abstract Shareable getShareable();
	protected abstract Collection<? extends Shareable> subscriptions0()
			throws DbException;
	protected abstract Collection<? extends Shareable> subscriptions1()
			throws DbException;
	protected abstract Class<? extends ConversationMessageReceivedEvent<? extends InvitationResponse>> getResponseReceivedEventClass();
	@Test
	public void testAutoDeclinedSharing() throws Exception {
		setAutoDeleteTimer(c0, contactId1From0, MIN_AUTO_DELETE_TIMER_MS);
		getSharingManager0().sendInvitation(
				getShareable().getId(), contactId1From0, "This shareable!");
		assertGroupCount(c0, contactId1From0, 1, 0);
		forEachHeader(c0, contactId1From0, 1, h -> {
			assertEquals(MIN_AUTO_DELETE_TIMER_MS, h.getAutoDeleteTimer());
		});
		sync0To1(1, true);
		ack1To0(1);
		waitForEvents(c0);
		assertGroupCount(c1, contactId0From1, 1, 1);
		forEachHeader(c1, contactId0From1, 1, h -> {
			assertEquals(MIN_AUTO_DELETE_TIMER_MS, h.getAutoDeleteTimer());
		});
		assertEquals(MIN_AUTO_DELETE_TIMER_MS,
				getAutoDeleteTimer(c0, contactId1From0));
		assertEquals(MIN_AUTO_DELETE_TIMER_MS,
				getAutoDeleteTimer(c1, contactId0From1));
		long timerLatency = MIN_AUTO_DELETE_TIMER_MS + BATCH_DELAY_MS;
		c0.getTimeTravel().addCurrentTimeMillis(timerLatency - 1);
		c1.getTimeTravel().addCurrentTimeMillis(timerLatency - 1);
		assertGroupCount(c0, contactId1From0, 1, 0);
		assertEquals(1, getMessageHeaders(c0, contactId1From0).size());
		assertGroupCount(c1, contactId0From1, 1, 1);
		assertEquals(1, getMessageHeaders(c1, contactId0From1).size());
		ConversationMessagesDeletedEvent event =
				assertEvent(c0, ConversationMessagesDeletedEvent.class,
						() -> c0.getTimeTravel().addCurrentTimeMillis(1)
				);
		c1.getTimeTravel().addCurrentTimeMillis(1);
		assertEquals(contactId1From0, event.getContactId());
		assertGroupCount(c0, contactId1From0, 0, 0);
		assertEquals(0, getMessageHeaders(c0, contactId1From0).size());
		assertGroupCount(c1, contactId0From1, 1, 1);
		assertEquals(1, getMessageHeaders(c1, contactId0From1).size());
		final MessageId messageId0 =
				getMessageHeaders(c1, contactId0From1).get(0).getId();
		markMessageRead(c1, contact0From1, messageId0);
		assertGroupCount(c1, contactId0From1, 1, 0);
		c0.getTimeTravel().addCurrentTimeMillis(timerLatency - 1);
		c1.getTimeTravel().addCurrentTimeMillis(timerLatency - 1);
		assertGroupCount(c0, contactId1From0, 0, 0);
		assertEquals(0, getMessageHeaders(c0, contactId1From0).size());
		assertGroupCount(c1, contactId0From1, 1, 0);
		assertEquals(1, getMessageHeaders(c1, contactId0From1).size());
		c0.getTimeTravel().addCurrentTimeMillis(1);
		ConversationMessageReceivedEvent<? extends InvitationResponse> e =
				assertEvent(c1, getResponseReceivedEventClass(),
						() -> c1.getTimeTravel().addCurrentTimeMillis(1)
				);
		assertEquals(contactId0From1, e.getContactId());
		assertGroupCount(c0, contactId1From0, 0, 0);
		assertEquals(0, getMessageHeaders(c0, contactId1From0).size());
		assertGroupCount(c1, contactId0From1, 1, 0);
		forEachHeader(c1, contactId0From1, 1, h -> {
			assertNotEquals(messageId0, h.getId());
			assertTrue(h instanceof InvitationResponse);
			assertEquals(h.getId(), e.getMessageHeader().getId());
			assertFalse(((InvitationResponse) h).wasAccepted());
			assertTrue(((InvitationResponse) h).isAutoDecline());
			assertEquals(MIN_AUTO_DELETE_TIMER_MS,
					h.getAutoDeleteTimer());
		});
		sync1To0(1, true);
		ack0To1(1);
		waitForEvents(c1);
		assertEquals(SHAREABLE, getSharingManager0().getSharingStatus(
				getShareable().getId(), contact1From0));
		c0.getTimeTravel().addCurrentTimeMillis(timerLatency - 1);
		c1.getTimeTravel().addCurrentTimeMillis(timerLatency - 1);
		assertGroupCount(c0, contactId1From0, 1, 1);
		assertEquals(1, getMessageHeaders(c0, contactId1From0).size());
		assertGroupCount(c1, contactId0From1, 1, 0);
		assertEquals(1, getMessageHeaders(c1, contactId0From1).size());
		c0.getTimeTravel().addCurrentTimeMillis(1);
		c1.getTimeTravel().addCurrentTimeMillis(1);
		assertGroupCount(c0, contactId1From0, 1, 1);
		assertEquals(1, getMessageHeaders(c0, contactId1From0).size());
		assertGroupCount(c1, contactId0From1, 0, 0);
		assertEquals(0, getMessageHeaders(c1, contactId0From1).size());
		MessageId messageId1 =
				getMessageHeaders(c0, contactId1From0).get(0).getId();
		markMessageRead(c0, contact1From0, messageId1);
		assertGroupCount(c0, contactId1From0, 1, 0);
		c0.getTimeTravel().addCurrentTimeMillis(timerLatency - 1);
		assertGroupCount(c0, contactId1From0, 1, 0);
		assertEquals(1, getMessageHeaders(c0, contactId1From0).size());
		c0.getTimeTravel().addCurrentTimeMillis(1);
		assertGroupCount(c0, contactId1From0, 0, 0);
		assertEquals(0, getMessageHeaders(c0, contactId1From0).size());
		assertEquals(SHAREABLE, getSharingManager0()
				.getSharingStatus(getShareable().getId(), contact1From0));
		getSharingManager0()
				.sendInvitation(getShareable().getId(), contactId1From0,
						"This shareable, please be quick!");
		sync0To1(1, true);
		assertGroupCount(c1, contactId0From1, 1, 1);
	}
	@Test
	public void testRespondAfterSenderDeletedInvitation() throws Exception {
		setAutoDeleteTimer(c0, contactId1From0, MIN_AUTO_DELETE_TIMER_MS);
		assertTrue(subscriptions0().contains(getShareable()));
		assertFalse(subscriptions1().contains(getShareable()));
		int expectedSubscriptions1 = subscriptions1().size() + 1;
		getSharingManager0().sendInvitation(
				getShareable().getId(), contactId1From0, "This shareable!");
		sync0To1(1, true);
		ack1To0(1);
		waitForEvents(c0);
		assertGroupCount(c1, contactId0From1, 1, 1);
		long timerLatency = MIN_AUTO_DELETE_TIMER_MS + BATCH_DELAY_MS;
		c0.getTimeTravel().addCurrentTimeMillis(timerLatency);
		c1.getTimeTravel().addCurrentTimeMillis(timerLatency);
		assertGroupCount(c0, contactId1From0, 0, 0);
		assertEquals(0, getMessageHeaders(c0, contactId1From0).size());
		assertGroupCount(c1, contactId0From1, 1, 1);
		assertEquals(1, getMessageHeaders(c1, contactId0From1).size());
		markMessageRead(c1, contact0From1,
				getMessageHeaders(c1, contactId0From1).get(0).getId());
		assertGroupCount(c1, contactId0From1, 1, 0);
		assertEquals(1, getSharingManager1().getInvitations().size());
		SharingInvitationItem invitation =
				getSharingManager1().getInvitations().iterator().next();
		assertEquals(getShareable(), invitation.getShareable());
		Contact c = contactManager1.getContact(contactId0From1);
		if (getShareable() instanceof Blog) {
			((SharingManager<Blog>) getSharingManager1()).respondToInvitation(
					(Blog) getShareable(), c, true);
		} else if (getShareable() instanceof Forum) {
			((SharingManager<Forum>) getSharingManager1()).respondToInvitation(
					(Forum) getShareable(), c, true);
		} else {
			fail();
		}
		sync1To0(1, true);
		ack0To1(1);
		waitForEvents(c1);
		assertGroupCount(c0, contactId1From0, 1, 1);
		assertGroupCount(c1, contactId0From1, 2, 0);
		markMessageRead(c0, contact1From0,
				getMessageHeaders(c0, contactId1From0).get(0).getId());
		assertGroupCount(c0, contactId1From0, 1, 0);
		assertGroupCount(c1, contactId0From1, 2, 0);
		InvitationResponse acceptMessage = (InvitationResponse)
				getMessageHeaders(c1, contactId0From1).get(1);
		assertEquals(acceptMessage.getShareableId(),
				((BaseGroup) getShareable()).getId());
		c0.getTimeTravel().addCurrentTimeMillis(timerLatency);
		c1.getTimeTravel().addCurrentTimeMillis(timerLatency);
		assertGroupCount(c0, contactId1From0, 0, 0);
		assertEquals(0, getMessageHeaders(c0, contactId1From0).size());
		assertGroupCount(c1, contactId0From1, 0, 0);
		assertEquals(0, getMessageHeaders(c1, contactId0From1).size());
		assertEquals(0, getSharingManager0().getInvitations().size());
		assertEquals(0, getSharingManager1().getInvitations().size());
		assertEquals(expectedSubscriptions1, subscriptions1().size());
	}
}