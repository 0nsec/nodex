package org.nodex.android;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.preference.PreferenceManager;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import org.nodex.core.BrambleAndroidEagerSingletons;
import org.nodex.core.BrambleAppComponent;
import org.nodex.core.BrambleCoreEagerSingletons;
import org.nodex.NodexCoreEagerSingletons;
import org.nodex.R;
import org.nodex.android.logging.CachingLogHandler;
import org.nodex.android.util.UiUtils;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import androidx.annotation.NonNull;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import static org.nodex.android.TestingConstants.IS_DEBUG_BUILD;
import static org.nodex.android.settings.DisplayFragment.PREF_THEME;
public class NodexApplicationImpl extends Application
		implements NodexApplication {
	private static final Logger LOG =
			getLogger(NodexApplicationImpl.class.getName());
	private AndroidComponent applicationComponent;
	private volatile SharedPreferences prefs;
	@Override
	protected void attachBaseContext(Context base) {
		if (prefs == null)
			prefs = PreferenceManager.getDefaultSharedPreferences(base);
		Localizer.initialize(prefs);
		super.attachBaseContext(
				Localizer.getInstance().setLocale(base));
		Localizer.getInstance().setLocale(this);
		setTheme(base, prefs);
	}
	@Override
	public void onCreate() {
		super.onCreate();
		if (IS_DEBUG_BUILD) enableStrictMode();
		applicationComponent = createApplicationComponent();
		UncaughtExceptionHandler exceptionHandler =
				applicationComponent.exceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
		Logger rootLogger = getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		for (Handler handler : handlers) rootLogger.removeHandler(handler);
		if (IS_DEBUG_BUILD) {
			rootLogger.addHandler(new LevelRaisingHandler(FINE, INFO));
			for (Handler handler : handlers) rootLogger.addHandler(handler);
		}
		CachingLogHandler logHandler = applicationComponent.logHandler();
		rootLogger.addHandler(logHandler);
		rootLogger.setLevel(IS_DEBUG_BUILD ? FINE : INFO);
		LOG.info("Created");
		EmojiManager.install(new GoogleEmojiProvider());
	}
	protected AndroidComponent createApplicationComponent() {
		AndroidComponent androidComponent = DaggerAndroidComponent.builder()
				.appModule(new AppModule(this))
				.build();
		BrambleCoreEagerSingletons.Helper
				.injectEagerSingletons(androidComponent);
		BrambleAndroidEagerSingletons.Helper
				.injectEagerSingletons(androidComponent);
		NodexCoreEagerSingletons.Helper.injectEagerSingletons(androidComponent);
		AndroidEagerSingletons.Helper.injectEagerSingletons(androidComponent);
		return androidComponent;
	}
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Localizer.getInstance().setLocale(this);
	}
	private void setTheme(Context ctx, SharedPreferences prefs) {
		String theme = prefs.getString(PREF_THEME, null);
		if (theme == null) {
			theme = getString(R.string.pref_theme_system_value);
			prefs.edit().putString(PREF_THEME, theme).apply();
		}
		UiUtils.setTheme(ctx, theme);
	}
	private void enableStrictMode() {
		ThreadPolicy.Builder threadPolicy = new ThreadPolicy.Builder();
		threadPolicy.detectAll();
		threadPolicy.penaltyLog();
		StrictMode.setThreadPolicy(threadPolicy.build());
		VmPolicy.Builder vmPolicy = new VmPolicy.Builder();
		vmPolicy.detectAll();
		vmPolicy.penaltyLog();
		StrictMode.setVmPolicy(vmPolicy.build());
	}
	@Override
	public BrambleAppComponent getBrambleAppComponent() {
		return applicationComponent;
	}
	@Override
	public AndroidComponent getApplicationComponent() {
		return applicationComponent;
	}
	@Override
	public SharedPreferences getDefaultSharedPreferences() {
		return prefs;
	}
	@Override
	public boolean isRunningInBackground() {
		RunningAppProcessInfo info = new RunningAppProcessInfo();
		ActivityManager.getMyMemoryState(info);
		return (info.importance != IMPORTANCE_FOREGROUND);
	}
	@Override
	public boolean isInstrumentationTest() {
		return false;
	}
}