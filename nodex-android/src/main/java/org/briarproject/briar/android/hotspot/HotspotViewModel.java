package org.nodex.android.hotspot;
import android.app.Application;
import android.net.Uri;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.lifecycle.IoExecutor;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.R;
import org.nodex.android.hotspot.HotspotManager.HotspotListener;
import org.nodex.android.hotspot.HotspotState.HotspotError;
import org.nodex.android.hotspot.HotspotState.HotspotStarted;
import org.nodex.android.hotspot.HotspotState.NetworkConfig;
import org.nodex.android.hotspot.HotspotState.StartingHotspot;
import org.nodex.android.hotspot.HotspotState.WebsiteConfig;
import org.nodex.android.hotspot.WebServerManager.WebServerListener;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.android.viewmodel.LiveEvent;
import org.nodex.android.viewmodel.MutableLiveEvent;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.IoUtils.copyAndClose;
import static org.nodex.BuildConfig.DEBUG;
import static org.nodex.BuildConfig.VERSION_NAME;
@NotNullByDefault
class HotspotViewModel extends DbViewModel
		implements HotspotListener, WebServerListener {
	private static final Logger LOG =
			getLogger(HotspotViewModel.class.getName());
	@IoExecutor
	private final Executor ioExecutor;
	private final AndroidNotificationManager notificationManager;
	private final HotspotManager hotspotManager;
	private final WebServerManager webServerManager;
	private final MutableLiveData<HotspotState> state =
			new MutableLiveData<>();
	private final MutableLiveData<Integer> peersConnected =
			new MutableLiveData<>();
	private final MutableLiveEvent<Uri> savedApkToUri =
			new MutableLiveEvent<>();
	@Nullable
	private volatile NetworkConfig networkConfig;
	@Inject
	HotspotViewModel(Application app,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			@IoExecutor Executor ioExecutor,
			HotspotManager hotspotManager,
			WebServerManager webServerManager,
			AndroidNotificationManager notificationManager) {
		super(app, dbExecutor, lifecycleManager, db, androidExecutor);
		this.ioExecutor = ioExecutor;
		this.notificationManager = notificationManager;
		this.hotspotManager = hotspotManager;
		this.hotspotManager.setHotspotListener(this);
		this.webServerManager = webServerManager;
		this.webServerManager.setListener(this);
	}
	@UiThread
	void startHotspot() {
		HotspotState s = state.getValue();
		if (s instanceof HotspotStarted) {
			HotspotStarted old = (HotspotStarted) s;
			state.setValue(new HotspotStarted(old.getNetworkConfig(),
					old.getWebsiteConfig()));
		} else {
			hotspotManager.startWifiP2pHotspot();
			notificationManager.showHotspotNotification();
		}
	}
	@UiThread
	private void stopHotspot() {
		ioExecutor.execute(webServerManager::stopWebServer);
		hotspotManager.stopWifiP2pHotspot();
		notificationManager.clearHotspotNotification();
	}
	@Override
	protected void onCleared() {
		super.onCleared();
		stopHotspot();
	}
	@Override
	public void onStartingHotspot() {
		state.setValue(new StartingHotspot());
	}
	@Override
	@IoExecutor
	public void onHotspotStarted(NetworkConfig networkConfig) {
		this.networkConfig = networkConfig;
		LOG.info("starting webserver");
		webServerManager.startWebServer();
	}
	@UiThread
	@Override
	public void onPeersUpdated(int peers) {
		peersConnected.setValue(peers);
	}
	@Override
	public void onHotspotError(String error) {
		if (LOG.isLoggable(WARNING)) {
			LOG.warning("Hotspot error: " + error);
		}
		state.postValue(new HotspotError(error));
		ioExecutor.execute(webServerManager::stopWebServer);
		notificationManager.clearHotspotNotification();
	}
	@Override
	@IoExecutor
	public void onWebServerStarted(WebsiteConfig websiteConfig) {
		NetworkConfig nc = requireNonNull(networkConfig);
		state.postValue(new HotspotStarted(nc, websiteConfig));
		networkConfig = null;
	}
	@Override
	@IoExecutor
	public void onWebServerError() {
		state.postValue(new HotspotError(getApplication()
				.getString(R.string.hotspot_error_web_server_start)));
		stopHotspot();
	}
	void exportApk(Uri uri) {
		try {
			OutputStream out = getApplication().getContentResolver()
					.openOutputStream(uri, "wt");
			writeApk(out, uri);
		} catch (FileNotFoundException e) {
			handleException(e);
		}
	}
	static String getApkFileName() {
		return "briar" + (DEBUG ? "-debug-" : "-") + VERSION_NAME + ".apk";
	}
	private void writeApk(OutputStream out, Uri uriToShare) {
		File apk = new File(getApplication().getPackageCodePath());
		ioExecutor.execute(() -> {
			try {
				FileInputStream in = new FileInputStream(apk);
				copyAndClose(in, out);
				savedApkToUri.postEvent(uriToShare);
			} catch (IOException e) {
				handleException(e);
			}
		});
	}
	LiveData<HotspotState> getState() {
		return state;
	}
	LiveData<Integer> getPeersConnectedEvent() {
		return peersConnected;
	}
	LiveEvent<Uri> getSavedApkToUri() {
		return savedApkToUri;
	}
}