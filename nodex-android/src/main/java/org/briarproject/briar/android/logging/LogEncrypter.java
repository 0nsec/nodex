package org.nodex.android.logging;
import org.nodex.core.util.AndroidUtils;
import org.nodex.nullsafety.NotNullByDefault;
import androidx.annotation.Nullable;
@NotNullByDefault
public interface LogEncrypter {
	@Nullable
	byte[] encryptLogs();
}