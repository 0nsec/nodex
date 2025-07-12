package org.nodex.android;
import org.nodex.BuildConfig;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.nodex.BuildConfig.BuildTimestamp;
public interface TestingConstants {
	boolean IS_DEBUG_BUILD = BuildConfig.DEBUG;
	boolean PREVENT_SCREENSHOTS = !IS_DEBUG_BUILD;
	long EXPIRY_DATE = IS_DEBUG_BUILD ?
			BuildTimestamp + DAYS.toMillis(90) : Long.MAX_VALUE;
}