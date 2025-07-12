package org.nodex.privategroup;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.core.test.TestDatabaseConfigModule;
import org.nodex.api.privategroup.GroupMember;
import org.nodex.api.privategroup.GroupMessage;
import org.nodex.api.privategroup.GroupMessageHeader;
import org.nodex.api.privategroup.JoinMessageHeader;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.invitation.GroupInvitationManager;
import org.nodex.test.NodexIntegrationTest;
import org.nodex.test.NodexIntegrationTestComponent;
import org.nodex.test.DaggerBriarIntegrationTestComponent;
import org.junit.Before;
import org.junit.Test;
import java.util.Collection;
import javax.annotation.Nullable;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.api.identity.AuthorInfo.Status.OURSELVES;
import static org.nodex.api.privategroup.Visibility.INVISIBLE;
import static org.nodex.api.privategroup.Visibility.REVEALED_BY_CONTACT;
import static org.nodex.api.privategroup.Visibility.REVEALED_BY_US;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
public class PrivateGroupIntegrationTest
		extends NodexIntegrationTest<NodexIntegrationTestComponent> {
	private GroupId groupId0;
	private PrivateGroup privateGroup0;
	private PrivateGroupManager groupManager0, groupManager1, groupManager2;
	private GroupInvitationManager groupInvitationManager0,
			groupInvitationManager1, groupInvitationManager2;
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		groupManager0 = c0.getPrivateGroupManager();
		groupManager1 = c1.getPrivateGroupManager();
		groupManager2 = c2.getPrivateGroupManager();
		groupInvitationManager0 = c0.getGroupInvitationManager();
		groupInvitationManager1 = c1.getGroupInvitationManager();
		groupInvitationManager2 = c2.getGroupInvitationManager();
		privateGroup0 =
				privateGroupFactory.createPrivateGroup("Test Group", author0);
		groupId0 = privateGroup0.getId();
		long joinTime = c0.getClock().currentTimeMillis();
		GroupMessage joinMsg0 = groupMessageFactory
				.createJoinMessage(groupId0, joinTime, author0);
		groupManager0.addPrivateGroup(privateGroup0, joinMsg0, true);
	}
	@Override
	protected void createComponents() {
		NodexIntegrationTestComponent component =
				DaggerBriarIntegrationTestComponent.builder().build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(component);
		component.inject(this);
		c0 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t0Dir))
				.build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(c0);
		c1 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t1Dir))
				.build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(c1);
		c2 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t2Dir))
				.build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(c2);
	}
	@Test
	public void testMembership() throws Exception {
		sendInvitation(contactId1From0, c0.getClock().currentTimeMillis(),
				"Hi!");
		Collection<GroupMember> members = groupManager0.getMembers(groupId0);
		assertEquals(1, members.size());
		assertEquals(author0, members.iterator().next().getAuthor());
		assertEquals(OURSELVES,
				members.iterator().next().getAuthorInfo().getStatus());
		sync0To1(1, true);
		groupInvitationManager1
				.respondToInvitation(contactId0From1, privateGroup0, true);
		sync1To0(1, true);
		sync0To1(2, true);
		sync1To0(1, true);
		members = groupManager0.getMembers(groupId0);
		assertEquals(2, members.size());
		for (GroupMember m : members) {
			if (m.getAuthorInfo().getStatus() == OURSELVES) {
				assertEquals(author0.getId(), m.getAuthor().getId());
			} else {
				assertEquals(author1.getId(), m.getAuthor().getId());
			}
		}
		members = groupManager1.getMembers(groupId0);
		assertEquals(2, members.size());
		for (GroupMember m : members) {
			if (m.getAuthorInfo().getStatus() == OURSELVES) {
				assertEquals(author1.getId(), m.getAuthor().getId());
			} else {
				assertEquals(author0.getId(), m.getAuthor().getId());
			}
		}
	}
	@Test
	public void testRevealContacts() throws Exception {
		sendInvitation(contactId1From0, c0.getClock().currentTimeMillis(),
				"Hi 1!");
		sendInvitation(contactId2From0, c0.getClock().currentTimeMillis(),
				"Hi 2!");
		sync0To1(1, true);
		sync0To2(1, true);
		groupInvitationManager1
				.respondToInvitation(contactId0From1, privateGroup0, true);
		groupInvitationManager2
				.respondToInvitation(contactId0From2, privateGroup0, true);
		sync1To0(1, true);
		sync2To0(1, true);
		sync0To1(2, true);
		assertEquals(2, groupManager1.getMembers(groupId0).size());
		sync1To0(1, true);
		assertEquals(2, groupManager0.getMembers(groupId0).size());
		sync0To2(3, true);
		assertEquals(3, groupManager2.getMembers(groupId0).size());
		sync2To0(1, true);
		assertEquals(3, groupManager0.getMembers(groupId0).size());
		sync0To1(1, true);
		assertEquals(3, groupManager1.getMembers(groupId0).size());
		addContacts1And2();
		assertEquals(INVISIBLE,
				getGroupMember(groupManager1, author2.getId()).getVisibility());
		assertEquals(INVISIBLE,
				getGroupMember(groupManager2, author1.getId()).getVisibility());
		assertNotNull(contactId2From1);
		groupInvitationManager1.revealRelationship(contactId2From1, groupId0);
		sync1To2(1, true);
		syncMessage(c2, c1, contactId1From2, 1, 3, 0, 1);
		assertEquals(REVEALED_BY_US,
				getGroupMember(groupManager1, author2.getId()).getVisibility());
		assertEquals(REVEALED_BY_CONTACT,
				getGroupMember(groupManager2, author1.getId()).getVisibility());
		long time = c2.getClock().currentTimeMillis();
		String text = "This is a test message!";
		MessageId previousMsgId = groupManager2.getPreviousMsgId(groupId0);
		GroupMessage msg = groupMessageFactory
				.createGroupMessage(groupId0, time, null, author2, text,
						previousMsgId);
		groupManager2.addLocalMessage(msg);
		Collection<GroupMessageHeader> headers =
				groupManager1.getHeaders(groupId0);
		assertEquals(3, headers.size());
		sync2To1(1, true);
		headers = groupManager1.getHeaders(groupId0);
		assertEquals(4, headers.size());
		boolean foundPost = false;
		for (GroupMessageHeader h : headers) {
			if (h instanceof JoinMessageHeader) continue;
			foundPost = true;
			assertEquals(time, h.getTimestamp());
			assertEquals(groupId0, h.getGroupId());
			assertEquals(author2.getId(), h.getAuthor().getId());
		}
		assertTrue(foundPost);
		sync1To0(1, true);
		headers = groupManager0.getHeaders(groupId0);
		assertEquals(4, headers.size());
	}
	private void sendInvitation(ContactId c, long timestamp,
			@Nullable String text) throws DbException {
		Contact contact = contactManager0.getContact(c);
		byte[] signature = groupInvitationFactory
				.signInvitation(contact, groupId0, timestamp,
						author0.getPrivateKey());
		groupInvitationManager0.sendInvitation(groupId0, c, text, timestamp,
				signature, NO_AUTO_DELETE_TIMER);
	}
	private GroupMember getGroupMember(PrivateGroupManager groupManager,
			AuthorId a) throws DbException {
		Collection<GroupMember> members = groupManager.getMembers(groupId0);
		for (GroupMember m : members) {
			if (m.getAuthor().getId().equals(a)) return m;
		}
		throw new AssertionError();
	}
}