package org.briarproject.briar.android.attachment.media;
import org.briarproject.nullsafety.NotNullByDefault;
import java.io.InputStream;
@NotNullByDefault
public interface ImageSizeCalculator {
	Size getSize(InputStream is, String contentType);
}