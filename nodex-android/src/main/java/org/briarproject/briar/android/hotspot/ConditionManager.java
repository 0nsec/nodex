package org.nodex.android.hotspot;
import android.provider.Settings;
import org.nodex.R;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.logging.Logger;
import androidx.core.util.Consumer;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
@NotNullByDefault
class ConditionManager extends AbstractConditionManager {
	private static final Logger LOG =
			getLogger(ConditionManager.class.getName());
	ConditionManager(Consumer<Boolean> permissionUpdateCallback) {
		super( permissionUpdateCallback);
	}
	@Override
	void onStart() {
	}
	private boolean areEssentialPermissionsGranted() {
		if (LOG.isLoggable(INFO)) {
			LOG.info(String.format("areEssentialPermissionsGranted(): " +
							"wifiManager.isWifiEnabled()? %b",
					wifiManager.isWifiEnabled()));
		}
		return wifiManager.isWifiEnabled();
	}
	@Override
	boolean checkAndRequestConditions() {
		if (areEssentialPermissionsGranted()) return true;
		if (!wifiManager.isWifiEnabled()) {
			if (wifiManager.setWifiEnabled(true)) {
				LOG.info("Enabled wifi");
				return true;
			}
			showRationale(ctx, R.string.wifi_settings_title,
					R.string.wifi_settings_request_enable_body,
					this::requestEnableWiFi,
					() -> permissionUpdateCallback.accept(false));
		}
		return false;
	}
	@Override
	String getWifiSettingsAction() {
		return Settings.ACTION_WIFI_SETTINGS;
	}
}