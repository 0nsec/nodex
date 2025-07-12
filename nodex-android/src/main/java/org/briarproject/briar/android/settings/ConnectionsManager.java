package org.nodex.android.settings;
import org.nodex.core.api.plugin.BluetoothConstants;
import org.nodex.core.api.plugin.LanTcpConstants;
import org.nodex.core.api.plugin.TorConstants;
import org.nodex.core.api.settings.Settings;
import org.nodex.core.api.settings.SettingsManager;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.concurrent.Executor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static org.nodex.core.api.plugin.Plugin.PREF_PLUGIN_ENABLE;
import static org.nodex.core.api.plugin.TorConstants.DEFAULT_PREF_TOR_MOBILE;
import static org.nodex.core.api.plugin.TorConstants.DEFAULT_PREF_TOR_NETWORK;
import static org.nodex.core.api.plugin.TorConstants.DEFAULT_PREF_TOR_ONLY_WHEN_CHARGING;
import static org.nodex.core.api.plugin.TorConstants.PREF_TOR_MOBILE;
import static org.nodex.core.api.plugin.TorConstants.PREF_TOR_NETWORK;
import static org.nodex.core.api.plugin.TorConstants.PREF_TOR_ONLY_WHEN_CHARGING;
import static org.nodex.android.settings.SettingsViewModel.BT_NAMESPACE;
import static org.nodex.android.settings.SettingsViewModel.TOR_NAMESPACE;
import static org.nodex.android.settings.SettingsViewModel.WIFI_NAMESPACE;
@NotNullByDefault
class ConnectionsManager {
	final ConnectionsStore btStore;
	final ConnectionsStore wifiStore;
	final ConnectionsStore torStore;
	private final MutableLiveData<Boolean> btEnabled = new MutableLiveData<>();
	private final MutableLiveData<Boolean> wifiEnabled =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> torEnabled = new MutableLiveData<>();
	private final MutableLiveData<String> torNetwork = new MutableLiveData<>();
	private final MutableLiveData<Boolean> torMobile = new MutableLiveData<>();
	private final MutableLiveData<Boolean> torCharging =
			new MutableLiveData<>();
	ConnectionsManager(SettingsManager settingsManager,
			Executor dbExecutor) {
		btStore =
				new ConnectionsStore(settingsManager, dbExecutor, BT_NAMESPACE);
		wifiStore = new ConnectionsStore(settingsManager, dbExecutor,
				WIFI_NAMESPACE);
		torStore = new ConnectionsStore(settingsManager, dbExecutor,
				TOR_NAMESPACE);
	}
	void updateBtSetting(Settings btSettings) {
		btEnabled.postValue(btSettings.getBoolean(PREF_PLUGIN_ENABLE,
				BluetoothConstants.DEFAULT_PREF_PLUGIN_ENABLE));
	}
	void updateWifiSettings(Settings wifiSettings) {
		wifiEnabled.postValue(wifiSettings.getBoolean(PREF_PLUGIN_ENABLE,
				LanTcpConstants.DEFAULT_PREF_PLUGIN_ENABLE));
	}
	void updateTorSettings(Settings settings) {
		torEnabled.postValue(settings.getBoolean(PREF_PLUGIN_ENABLE,
				TorConstants.DEFAULT_PREF_PLUGIN_ENABLE));
		int torNetworkSetting = settings.getInt(PREF_TOR_NETWORK,
				DEFAULT_PREF_TOR_NETWORK);
		torNetwork.postValue(Integer.toString(torNetworkSetting));
		torMobile.postValue(settings.getBoolean(PREF_TOR_MOBILE,
				DEFAULT_PREF_TOR_MOBILE));
		torCharging.postValue(settings.getBoolean(PREF_TOR_ONLY_WHEN_CHARGING,
				DEFAULT_PREF_TOR_ONLY_WHEN_CHARGING));
	}
	LiveData<Boolean> btEnabled() {
		return btEnabled;
	}
	LiveData<Boolean> wifiEnabled() {
		return wifiEnabled;
	}
	LiveData<Boolean> torEnabled() {
		return torEnabled;
	}
	LiveData<String> torNetwork() {
		return torNetwork;
	}
	LiveData<Boolean> torMobile() {
		return torMobile;
	}
	LiveData<Boolean> torCharging() {
		return torCharging;
	}
}