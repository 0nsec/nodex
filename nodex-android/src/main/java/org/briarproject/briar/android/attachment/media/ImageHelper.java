package org.nodex.android.attachment.media;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.InputStream;
import androidx.annotation.Nullable;
@NotNullByDefault
public interface ImageHelper {
	DecodeResult decodeStream(InputStream is);
	@Nullable
	String getExtensionFromMimeType(String mimeType);
	class DecodeResult {
		final int width;
		final int height;
		final String mimeType;
		DecodeResult(int width, int height, String mimeType) {
			this.width = width;
			this.height = height;
			this.mimeType = mimeType;
		}
	}
}