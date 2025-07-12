package org.nodex.introduction;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfList;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageFactory;
import org.nodex.core.test.BrambleMockTestCase;
import org.jmock.Expectations;
import org.junit.Test;
import static org.nodex.core.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
import static org.nodex.core.test.TestUtils.getAuthor;
import static org.nodex.core.test.TestUtils.getMessage;
import static org.nodex.core.test.TestUtils.getRandomId;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.nodex.api.introduction.IntroductionConstants.MAX_INTRODUCTION_TEXT_LENGTH;
import static org.nodex.introduction.MessageType.REQUEST;
public class MessageEncoderTest extends BrambleMockTestCase {
	private final ClientHelper clientHelper = context.mock(ClientHelper.class);
	private final MessageFactory messageFactory =
			context.mock(MessageFactory.class);
	private final MessageEncoder messageEncoder =
			new MessageEncoderImpl(clientHelper, messageFactory);
	private final GroupId groupId = new GroupId(getRandomId());
	private final Message message =
			getMessage(groupId, MAX_MESSAGE_BODY_LENGTH);
	private final long timestamp = message.getTimestamp();
	private final byte[] body = message.getBody();
	private final Author author = getAuthor();
	private final BdfList authorList = new BdfList();
	private final String text = getRandomString(MAX_INTRODUCTION_TEXT_LENGTH);
	@Test
	public void testEncodeRequestMessage() throws FormatException {
		context.checking(new Expectations() {{
			oneOf(clientHelper).toList(author);
			will(returnValue(authorList));
		}});
		expectCreateMessage(
				BdfList.of(REQUEST.getValue(), null, authorList, text));
		messageEncoder.encodeRequestMessage(groupId, timestamp, null,
				author, text);
	}
	private void expectCreateMessage(BdfList bodyList) throws FormatException {
		context.checking(new Expectations() {{
			oneOf(clientHelper).toByteArray(bodyList);
			will(returnValue(body));
			oneOf(messageFactory).createMessage(groupId, timestamp, body);
			will(returnValue(message));
		}});
	}
}