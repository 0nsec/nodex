package org.nodex.android.hotspot;
import android.provider.Settings;
import org.nodex.R;
import org.nodex.android.util.Permission;
import org.nodex.android.util.PermissionUtils;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.logging.Logger;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.util.Consumer;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.os.Build.VERSION.SDK_INT;
import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;
import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import static org.nodex.android.util.PermissionUtils.isLocationEnabledForWiFi;
import static org.nodex.android.util.PermissionUtils.showLocationDialog;
@RequiresApi(29)
@NotNullByDefault
class ConditionManager29 extends AbstractConditionManager {
	private static final Logger LOG =
			getLogger(ConditionManager29.class.getName());
	private Permission locationPermission = Permission.UNKNOWN;
	private final ActivityResultLauncher<String> locationRequest;
	ConditionManager29(ActivityResultCaller arc,
			Consumer<Boolean> permissionUpdateCallback) {
		super(permissionUpdateCallback);
		locationRequest = arc.registerForActivityResult(
				new RequestPermission(), granted -> {
					onRequestPermissionResult(granted);
					permissionUpdateCallback.accept(TRUE.equals(granted));
				});
	}
	@Override
	void onStart() {
		locationPermission = Permission.UNKNOWN;
	}
	private boolean areEssentialPermissionsGranted() {
		boolean isWifiEnabled = wifiManager.isWifiEnabled();
		boolean isLocationEnabled = isLocationEnabledForWiFi(ctx);
		if (LOG.isLoggable(INFO)) {
			LOG.info(String.format("areEssentialPermissionsGranted(): " +
							"locationPermission? %s, " +
							"wifiManager.isWifiEnabled()? %b" +
							"isLocationEnabled? %b",
					locationPermission, isWifiEnabled, isLocationEnabled));
		}
		return locationPermission == Permission.GRANTED && isWifiEnabled &&
				isLocationEnabled;
	}
	@Override
	boolean checkAndRequestConditions() {
		if (areEssentialPermissionsGranted()) return true;
		if (locationPermission == Permission.UNKNOWN) {
			requestPermissions();
			return false;
		}
		if (!isLocationEnabledForWiFi(ctx)) {
			showLocationDialog(ctx, false);
			return false;
		}
		if (locationPermission == Permission.PERMANENTLY_DENIED) {
			int res = SDK_INT >= 31 ?
					R.string.permission_hotspot_location_denied_precise_body :
					R.string.permission_hotspot_location_denied_body;
			PermissionUtils.showDenialDialog(ctx,
					R.string.permission_location_title, res,
					() -> permissionUpdateCallback.accept(false));
			return false;
		}
		if (locationPermission == Permission.SHOW_RATIONALE) {
			int res = SDK_INT >= 31 ?
					R.string.permission_hotspot_location_request_precise_body :
					R.string.permission_hotspot_location_request_body;
			showRationale(ctx, R.string.permission_location_title, res,
					this::requestPermissions,
					() -> permissionUpdateCallback.accept(false));
			return false;
		}
		if (!wifiManager.isWifiEnabled()) {
			showRationale(ctx, R.string.wifi_settings_title,
					R.string.wifi_settings_request_enable_body,
					this::requestEnableWiFi,
					() -> permissionUpdateCallback.accept(false));
			return false;
		}
		return false;
	}
	@Override
	String getWifiSettingsAction() {
		return Settings.Panel.ACTION_WIFI;
	}
	private void onRequestPermissionResult(@Nullable Boolean granted) {
		if (granted != null && granted) {
			locationPermission = Permission.GRANTED;
		} else if (shouldShowRequestPermissionRationale(ctx,
				ACCESS_FINE_LOCATION)) {
			locationPermission = Permission.SHOW_RATIONALE;
		} else {
			locationPermission = Permission.PERMANENTLY_DENIED;
		}
	}
	private void requestPermissions() {
		locationRequest.launch(ACCESS_FINE_LOCATION);
	}
}