package org.briarproject.briar.android;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import org.briarproject.nullsafety.NotNullByDefault;
import java.util.Locale;
import javax.annotation.Nullable;
import static android.os.Build.VERSION.SDK_INT;
import static org.briarproject.briar.android.settings.DisplayFragment.PREF_LANGUAGE;
@NotNullByDefault
public class Localizer {
	@Nullable
	private static Localizer INSTANCE;
	private final Locale systemLocale;
	private final Locale locale;
	private Localizer(SharedPreferences sharedPreferences) {
		this(Locale.getDefault(), getLocaleFromTag(
				sharedPreferences.getString(PREF_LANGUAGE, "default")));
	}
	private Localizer(Locale systemLocale, @Nullable Locale userLocale) {
		this.systemLocale = systemLocale;
		if (userLocale == null) locale = systemLocale;
		else locale = userLocale;
	}
	public static synchronized void initialize(SharedPreferences prefs) {
		if (INSTANCE == null)
			INSTANCE = new Localizer(prefs);
	}
	public static synchronized void reinitialize() {
		if (INSTANCE != null)
			INSTANCE = new Localizer(INSTANCE.systemLocale, null);
	}
	public static synchronized Localizer getInstance() {
		if (INSTANCE == null)
			throw new IllegalStateException("Localizer not initialized");
		return INSTANCE;
	}
	@Nullable
	public static Locale getLocaleFromTag(String tag) {
		if (tag.equals("default")) return null;
		return Locale.forLanguageTag(tag);
	}
	public Context setLocale(Context context) {
		Resources res = context.getResources();
		Configuration conf = res.getConfiguration();
		Locale currentLocale;
		if (SDK_INT >= 24) {
			currentLocale = conf.getLocales().get(0);
		} else
			currentLocale = conf.locale;
		if (locale.equals(currentLocale))
			return context;
		Locale.setDefault(locale);
		conf.setLocale(locale);
		context = context.createConfigurationContext(conf);
		res.updateConfiguration(conf, res.getDisplayMetrics());
		return context;
	}
}