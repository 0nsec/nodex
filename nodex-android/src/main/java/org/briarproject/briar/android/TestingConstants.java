package org.briarproject.briar.android;
import org.briarproject.briar.BuildConfig;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.briarproject.briar.BuildConfig.BuildTimestamp;
public interface TestingConstants {
	boolean IS_DEBUG_BUILD = BuildConfig.DEBUG;
	boolean PREVENT_SCREENSHOTS = !IS_DEBUG_BUILD;
	long EXPIRY_DATE = IS_DEBUG_BUILD ?
			BuildTimestamp + DAYS.toMillis(90) : Long.MAX_VALUE;
}