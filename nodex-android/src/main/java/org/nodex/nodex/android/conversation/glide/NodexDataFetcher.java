package org.nodex.android.conversation.glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.api.attachment.Attachment;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.attachment.AttachmentReader;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import static com.bumptech.glide.load.DataSource.LOCAL;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.IoUtils.tryToClose;
@NotNullByDefault
class NodexDataFetcher implements DataFetcher<InputStream> {
	private final static Logger LOG =
			getLogger(NodexDataFetcher.class.getName());
	private final AttachmentReader attachmentReader;
	@DatabaseExecutor
	private final Executor dbExecutor;
	private final AttachmentHeader attachmentHeader;
	@Nullable
	private volatile InputStream inputStream;
	private volatile boolean cancel = false;
	@Inject
	NodexDataFetcher(AttachmentReader attachmentReader,
			@DatabaseExecutor Executor dbExecutor,
			AttachmentHeader attachmentHeader) {
		this.attachmentReader = attachmentReader;
		this.dbExecutor = dbExecutor;
		this.attachmentHeader = attachmentHeader;
	}
	@Override
	public void loadData(Priority priority,
			DataCallback<? super InputStream> callback) {
		dbExecutor.execute(() -> {
			if (cancel) return;
			try {
				Attachment a = attachmentReader.getAttachment(attachmentHeader);
				inputStream = a.getStream();
				callback.onDataReady(inputStream);
			} catch (DbException e) {
				callback.onLoadFailed(e);
			}
		});
	}
	@Override
	public void cleanup() {
		tryToClose(inputStream, LOG, WARNING);
	}
	@Override
	public void cancel() {
		cancel = true;
	}
	@Override
	public Class<InputStream> getDataClass() {
		return InputStream.class;
	}
	@Override
	public DataSource getDataSource() {
		return LOCAL;
	}
}