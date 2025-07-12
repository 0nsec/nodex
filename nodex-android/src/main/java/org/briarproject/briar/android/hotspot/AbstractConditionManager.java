package org.nodex.android.hotspot;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.nodex.R;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentActivity;
import static android.content.Context.WIFI_SERVICE;
import static android.widget.Toast.LENGTH_LONG;
abstract class AbstractConditionManager {
	final Consumer<Boolean> permissionUpdateCallback;
	protected FragmentActivity ctx;
	WifiManager wifiManager;
	private ActivityResultLauncher<Intent> wifiRequest;
	AbstractConditionManager(Consumer<Boolean> permissionUpdateCallback) {
		this.permissionUpdateCallback = permissionUpdateCallback;
	}
	void init(FragmentActivity ctx) {
		this.ctx = ctx;
		wifiManager = (WifiManager) ctx.getApplicationContext()
				.getSystemService(WIFI_SERVICE);
		wifiRequest = ctx.registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				result -> permissionUpdateCallback
						.accept(wifiManager.isWifiEnabled()));
	}
	abstract void onStart();
	abstract boolean checkAndRequestConditions();
	abstract String getWifiSettingsAction();
	void showRationale(Context ctx, @StringRes int title,
			@StringRes int body, Runnable onContinueClicked,
			Runnable onDismiss) {
		MaterialAlertDialogBuilder builder =
				new MaterialAlertDialogBuilder(ctx);
		builder.setTitle(title);
		builder.setMessage(body);
		builder.setNeutralButton(R.string.continue_button,
				(dialog, which) -> onContinueClicked.run());
		builder.setOnDismissListener(dialog -> onDismiss.run());
		builder.show();
	}
	void requestEnableWiFi() {
		try {
			wifiRequest.launch(new Intent(getWifiSettingsAction()));
		} catch (ActivityNotFoundException e) {
			Toast.makeText(ctx, R.string.error_start_activity, LENGTH_LONG)
					.show();
		}
	}
}