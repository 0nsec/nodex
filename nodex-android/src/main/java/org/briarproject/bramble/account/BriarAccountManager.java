package org.nodex.core.account;
import android.app.Application;
import android.content.SharedPreferences;
import org.nodex.core.api.crypto.CryptoComponent;
import org.nodex.core.api.db.DatabaseConfig;
import org.nodex.core.api.identity.IdentityManager;
import org.nodex.R;
import org.nodex.android.Localizer;
import org.nodex.android.util.UiUtils;
import javax.inject.Inject;
class BriarAccountManager extends AndroidAccountManager {
	@Inject
	BriarAccountManager(DatabaseConfig databaseConfig, CryptoComponent crypto,
			IdentityManager identityManager, SharedPreferences prefs,
			Application app) {
		super(databaseConfig, crypto, identityManager, prefs, app);
	}
	@Override
	public void deleteAccount() {
		synchronized (stateChangeLock) {
			super.deleteAccount();
			Localizer.reinitialize();
			UiUtils.setTheme(appContext,
					appContext.getString(R.string.pref_theme_system_value));
		}
	}
}