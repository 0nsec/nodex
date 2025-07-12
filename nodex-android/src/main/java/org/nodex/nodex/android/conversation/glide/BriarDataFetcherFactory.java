package org.nodex.android.conversation.glide;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.attachment.AttachmentReader;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.concurrent.Executor;
import javax.inject.Inject;
@NotNullByDefault
public class BriarDataFetcherFactory {
	private final AttachmentReader attachmentReader;
	@DatabaseExecutor
	private final Executor dbExecutor;
	@Inject
	public BriarDataFetcherFactory(AttachmentReader attachmentReader,
			@DatabaseExecutor Executor dbExecutor) {
		this.attachmentReader = attachmentReader;
		this.dbExecutor = dbExecutor;
	}
	BriarDataFetcher createBriarDataFetcher(AttachmentHeader model) {
		return new BriarDataFetcher(attachmentReader, dbExecutor, model);
	}
}