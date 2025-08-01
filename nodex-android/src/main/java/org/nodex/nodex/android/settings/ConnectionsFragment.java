package org.nodex.android.settings;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import org.nodex.R;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.Map;
import javax.inject.Inject;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.os.Build.VERSION.SDK_INT;
import static org.nodex.android.AppModule.getAndroidComponent;
import static org.nodex.android.settings.SettingsActivity.enableAndPersist;
import static org.nodex.android.util.PermissionUtils.areBluetoothPermissionsGranted;
import static org.nodex.android.util.PermissionUtils.requestBluetoothPermissions;
import static org.nodex.android.util.PermissionUtils.showDenialDialog;
import static org.nodex.android.util.PermissionUtils.showRationale;
import static org.nodex.android.util.PermissionUtils.wasGrantedBluetoothPermissions;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ConnectionsFragment extends PreferenceFragmentCompat {
	static final String PREF_KEY_BLUETOOTH = "pref_key_bluetooth";
	static final String PREF_KEY_WIFI = "pref_key_wifi";
	static final String PREF_KEY_TOR_ENABLE = "pref_key_tor_enable";
	static final String PREF_KEY_TOR_NETWORK = "pref_key_tor_network";
	static final String PREF_KEY_TOR_MOBILE_DATA =
			"pref_key_tor_mobile_data";
	static final String PREF_KEY_TOR_ONLY_WHEN_CHARGING =
			"pref_key_tor_only_when_charging";
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private SettingsViewModel viewModel;
	private ConnectionsManager connectionsManager;
	private SwitchPreferenceCompat enableBluetooth;
	private SwitchPreferenceCompat enableWifi;
	private SwitchPreferenceCompat enableTor;
	private ListPreference torNetwork;
	private SwitchPreferenceCompat torMobile;
	private SwitchPreferenceCompat torOnlyWhenCharging;
	@RequiresApi(31)
	private final ActivityResultLauncher<String[]> requestPermissionLauncher =
			registerForActivityResult(new RequestMultiplePermissions(),
					this::handleBtPermissionResult);
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		getAndroidComponent(context).inject(this);
		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
				.get(SettingsViewModel.class);
		connectionsManager = viewModel.connectionsManager;
	}
	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.settings_connections);
		enableBluetooth = findPreference(PREF_KEY_BLUETOOTH);
		enableWifi = findPreference(PREF_KEY_WIFI);
		enableTor = findPreference(PREF_KEY_TOR_ENABLE);
		torNetwork = findPreference(PREF_KEY_TOR_NETWORK);
		torMobile = findPreference(PREF_KEY_TOR_MOBILE_DATA);
		torOnlyWhenCharging = findPreference(PREF_KEY_TOR_ONLY_WHEN_CHARGING);
		torNetwork.setSummaryProvider(viewModel.torSummaryProvider);
		if (SDK_INT >= 31) {
			enableBluetooth.setOnPreferenceChangeListener((p, value) -> {
				FragmentActivity ctx = requireActivity();
				if (areBluetoothPermissionsGranted(ctx)) {
					return true;
				} else if (shouldShowRequestPermissionRationale(
						BLUETOOTH_CONNECT)) {
					showRationale(ctx, R.string.permission_bluetooth_title,
							R.string.permission_bluetooth_body,
							this::requestBtPermissions);
					return false;
				} else {
					requestBtPermissions();
					return false;
				}
			});
		}
		enableBluetooth.setPreferenceDataStore(connectionsManager.btStore);
		enableWifi.setPreferenceDataStore(connectionsManager.wifiStore);
		enableTor.setPreferenceDataStore(connectionsManager.torStore);
		torNetwork.setPreferenceDataStore(connectionsManager.torStore);
		torMobile.setPreferenceDataStore(connectionsManager.torStore);
		torOnlyWhenCharging.setPreferenceDataStore(connectionsManager.torStore);
	}
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
		connectionsManager.btEnabled().observe(lifecycleOwner, enabled -> {
			enableBluetooth.setChecked(enabled);
			enableAndPersist(enableBluetooth);
		});
		connectionsManager.wifiEnabled().observe(lifecycleOwner, enabled -> {
			enableWifi.setChecked(enabled);
			enableAndPersist(enableWifi);
		});
		connectionsManager.torEnabled().observe(lifecycleOwner, enabled -> {
			enableTor.setChecked(enabled);
			enableAndPersist(enableTor);
		});
		connectionsManager.torNetwork().observe(lifecycleOwner, value -> {
			torNetwork.setValue(value);
			enableAndPersist(torNetwork);
		});
		connectionsManager.torMobile().observe(lifecycleOwner, enabled -> {
			torMobile.setChecked(enabled);
			enableAndPersist(torMobile);
		});
		connectionsManager.torCharging().observe(lifecycleOwner, enabled -> {
			torOnlyWhenCharging.setChecked(enabled);
			enableAndPersist(torOnlyWhenCharging);
		});
	}
	@Override
	public void onStart() {
		super.onStart();
		requireActivity().setTitle(R.string.network_settings_title);
	}
	@RequiresApi(31)
	private void requestBtPermissions() {
		requestBluetoothPermissions(requestPermissionLauncher);
	}
	@RequiresApi(31)
	private void handleBtPermissionResult(Map<String, Boolean> grantedMap) {
		if (wasGrantedBluetoothPermissions(requireActivity(), grantedMap)) {
			enableBluetooth.setChecked(true);
		} else {
			showDenialDialog(requireActivity(),
					R.string.permission_bluetooth_title,
					R.string.permission_bluetooth_denied_body);
		}
	}
}