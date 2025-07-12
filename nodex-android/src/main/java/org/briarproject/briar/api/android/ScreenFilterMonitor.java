package org.briarproject.briar.api.android;
import org.briarproject.nullsafety.NotNullByDefault;
import java.util.Collection;
import androidx.annotation.UiThread;
@NotNullByDefault
public interface ScreenFilterMonitor {
	@UiThread
	Collection<AppDetails> getApps();
	@UiThread
	void allowApps(Collection<String> packageNames);
	class AppDetails {
		public final String name;
		public final String packageName;
		public AppDetails(String name, String packageName) {
			this.name = name;
			this.packageName = packageName;
		}
	}
}