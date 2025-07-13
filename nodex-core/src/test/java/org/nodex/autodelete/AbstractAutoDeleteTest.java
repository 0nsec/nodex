package org.nodex.autodelete;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.core.system.TimeTravelModule;
import org.nodex.core.test.TestDatabaseConfigModule;
import org.nodex.api.autodelete.AutoDeleteManager;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.conversation.ConversationManager.ConversationClient;
import org.nodex.api.conversation.ConversationMessageHeader;
import org.nodex.test.NodexIntegrationTest;
import org.nodex.test.NodexIntegrationTestComponent;
import org.nodex.test.DaggerNodexIntegrationTestComponent;
import org.junit.Before;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static java.util.Collections.sort;
import static org.nodex.core.api.cleanup.CleanupManager.BATCH_DELAY_MS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
public abstract class AbstractAutoDeleteTest extends
		NodexIntegrationTest<NodexIntegrationTestComponent> {
	protected final long startTime = System.currentTimeMillis();
	protected abstract ConversationClient getConversationClient(
			NodexIntegrationTestComponent component);
	@Override
	protected void createComponents() {
		NodexIntegrationTestComponent component =
				DaggerNodexIntegrationTestComponent.builder().build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(component);
		component.inject(this);
		c0 = DaggerNodexIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t0Dir))
				.timeTravelModule(new TimeTravelModule(true))
				.build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(c0);
		c1 = DaggerNodexIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t1Dir))
				.timeTravelModule(new TimeTravelModule(true))
				.build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(c1);
		c2 = DaggerNodexIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t2Dir))
				.timeTravelModule(new TimeTravelModule(true))
				.build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(c2);
		try {
			c0.getTimeTravel().setCurrentTimeMillis(startTime);
			c1.getTimeTravel().setCurrentTimeMillis(startTime + 1);
			c2.getTimeTravel().setCurrentTimeMillis(startTime + 2);
		} catch (InterruptedException e) {
			fail();
		}
	}
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		c0.getTimeTravel().addCurrentTimeMillis(BATCH_DELAY_MS);
		c1.getTimeTravel().addCurrentTimeMillis(BATCH_DELAY_MS);
		c2.getTimeTravel().addCurrentTimeMillis(BATCH_DELAY_MS);
	}
	protected List<ConversationMessageHeader> getMessageHeaders(
			NodexIntegrationTestComponent component, ContactId contactId)
			throws Exception {
		DatabaseComponent db = component.getDatabaseComponent();
		ConversationClient conversationClient =
				getConversationClient(component);
		return sortHeaders(db.transactionWithResult(true, txn ->
				conversationClient.getMessageHeaders(txn, contactId)));
	}
	@SuppressWarnings({"UseCompareMethod", "Java8ListSort"})
	protected List<ConversationMessageHeader> sortHeaders(
			Collection<ConversationMessageHeader> in) {
		List<ConversationMessageHeader> out = new ArrayList<>(in);
		sort(out, (a, b) ->
				Long.valueOf(a.getTimestamp()).compareTo(b.getTimestamp()));
		return out;
	}
	@FunctionalInterface
	protected interface HeaderConsumer {
		void accept(ConversationMessageHeader header) throws DbException;
	}
	protected void forEachHeader(NodexIntegrationTestComponent component,
			ContactId contactId, int size, HeaderConsumer consumer)
			throws Exception {
		List<ConversationMessageHeader> headers =
				getMessageHeaders(component, contactId);
		assertEquals(size, headers.size());
		for (ConversationMessageHeader h : headers) consumer.accept(h);
	}
	protected long getAutoDeleteTimer(NodexIntegrationTestComponent component,
			ContactId contactId) throws DbException {
		DatabaseComponent db = component.getDatabaseComponent();
		AutoDeleteManager autoDeleteManager = component.getAutoDeleteManager();
		return db.transactionWithResult(false,
				txn -> autoDeleteManager.getAutoDeleteTimer(txn, contactId));
	}
	protected void markMessageRead(NodexIntegrationTestComponent component,
			Contact contact, MessageId messageId) throws Exception {
		ConversationManager conversationManager =
				component.getConversationManager();
		ConversationClient conversationClient =
				getConversationClient(component);
		GroupId groupId = conversationClient.getContactGroup(contact).getId();
		conversationManager.setReadFlag(groupId, messageId, true);
		waitForEvents(component);
	}
	protected void assertGroupCount(NodexIntegrationTestComponent component,
			ContactId contactId, int messageCount, int unreadCount)
			throws DbException {
		DatabaseComponent db = component.getDatabaseComponent();
		ConversationClient conversationClient =
				getConversationClient(component);
		GroupCount gc = db.transactionWithResult(true, txn ->
				conversationClient.getGroupCount(txn, contactId));
		assertEquals("messageCount", messageCount, gc.getMsgCount());
		assertEquals("unreadCount", unreadCount, gc.getUnreadCount());
	}
}