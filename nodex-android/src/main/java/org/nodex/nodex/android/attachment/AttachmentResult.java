package org.nodex.android.attachment;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class AttachmentResult {
	private final Collection<AttachmentItemResult> itemResults;
	private final boolean finished;
	public AttachmentResult(Collection<AttachmentItemResult> itemResults,
			boolean finished) {
		this.itemResults = itemResults;
		this.finished = finished;
	}
	public Collection<AttachmentItemResult> getItemResults() {
		return itemResults;
	}
	public boolean isFinished() {
		return finished;
	}
}