package org.nodex.android.attachment.media;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.InputStream;
@NotNullByDefault
public interface ImageSizeCalculator {
	Size getSize(InputStream is, String contentType);
}