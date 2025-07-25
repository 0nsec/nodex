package org.nodex.android.reporting;
import android.app.Application;
import android.os.Process;
import android.util.Log;
import org.nodex.android.logging.LogEncrypter;
import org.nodex.nullsafety.NotNullByDefault;
import java.lang.Thread.UncaughtExceptionHandler;
import javax.inject.Inject;
import static org.nodex.android.TestingConstants.IS_DEBUG_BUILD;
import static org.nodex.android.util.UiUtils.startDevReportActivity;
@NotNullByDefault
class NodexExceptionHandler implements UncaughtExceptionHandler {
	private final Application app;
	private final LogEncrypter logEncrypter;
	private final long appStartTime;
	@Inject
	NodexExceptionHandler(Application app, LogEncrypter logEncrypter) {
		this.app = app;
		this.logEncrypter = logEncrypter;
		appStartTime = System.currentTimeMillis();
	}
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (IS_DEBUG_BUILD) Log.w("Uncaught exception", e);
		byte[] logKey = logEncrypter.encryptLogs();
		startDevReportActivity(app.getApplicationContext(),
				CrashReportActivity.class, e, appStartTime, logKey, null);
		Process.killProcess(Process.myPid());
		System.exit(10);
	}
}