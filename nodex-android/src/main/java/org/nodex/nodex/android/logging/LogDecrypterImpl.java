package org.nodex.android.logging;
import org.nodex.core.api.crypto.SecretKey;
import org.nodex.core.api.reporting.DevConfig;
import org.nodex.core.api.transport.StreamReaderFactory;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logException;
@NotNullByDefault
class LogDecrypterImpl implements LogDecrypter {
	private static final Logger LOG =
			getLogger(LogDecrypterImpl.class.getName());
	private final DevConfig devConfig;
	private final StreamReaderFactory streamReaderFactory;
	@Inject
	LogDecrypterImpl(DevConfig devConfig,
			StreamReaderFactory streamReaderFactory) {
		this.devConfig = devConfig;
		this.streamReaderFactory = streamReaderFactory;
	}
	@Nullable
	@Override
	public String decryptLogs(@Nullable byte[] logKey) {
		if (logKey == null) return null;
		SecretKey key = new SecretKey(logKey);
		File logFile = devConfig.getLogcatFile();
		try (InputStream in = new FileInputStream(logFile)) {
			InputStream reader =
					streamReaderFactory.createLogStreamReader(in, key);
			Scanner s = new Scanner(reader);
			StringBuilder sb = new StringBuilder();
			while (s.hasNextLine()) sb.append(s.nextLine()).append("\n");
			s.close();
			return sb.toString();
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			return null;
		} finally {
			logFile.delete();
		}
	}
}