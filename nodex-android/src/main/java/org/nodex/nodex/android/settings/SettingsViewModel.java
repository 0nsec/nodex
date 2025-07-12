package org.nodex.android.settings;
import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import android.widget.Toast;
import org.nodex.core.api.FeatureFlags;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.identity.IdentityManager;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.core.api.lifecycle.IoExecutor;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.plugin.BluetoothConstants;
import org.nodex.core.api.plugin.LanTcpConstants;
import org.nodex.core.api.plugin.TorConstants;
import org.nodex.core.api.settings.Settings;
import org.nodex.core.api.settings.SettingsManager;
import org.nodex.core.api.settings.event.SettingsUpdatedEvent;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.R;
import org.nodex.android.attachment.UnsupportedMimeTypeException;
import org.nodex.android.attachment.media.ImageCompressor;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.api.avatar.AvatarManager;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorManager;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import org.nodex.onionwrapper.CircumventionProvider;
import org.nodex.onionwrapper.LocationUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.AnyThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.Arrays.asList;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.AndroidUtils.getSupportedImageContentTypes;
import static org.nodex.core.util.LogUtils.logDuration;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.core.util.LogUtils.now;
import static org.nodex.android.settings.SecurityFragment.PREF_SCREEN_LOCK;
import static org.nodex.android.settings.SecurityFragment.PREF_SCREEN_LOCK_TIMEOUT;
import static org.nodex.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class SettingsViewModel extends DbViewModel implements EventListener {
	private final static Logger LOG =
			getLogger(SettingsViewModel.class.getName());
	static final String BT_NAMESPACE =
			BluetoothConstants.ID.getString();
	static final String WIFI_NAMESPACE = LanTcpConstants.ID.getString();
	static final String TOR_NAMESPACE = TorConstants.ID.getString();
	private final SettingsManager settingsManager;
	private final IdentityManager identityManager;
	private final EventBus eventBus;
	private final AvatarManager avatarManager;
	private final AuthorManager authorManager;
	private final ImageCompressor imageCompressor;
	private final Executor ioExecutor;
	private final FeatureFlags featureFlags;
	final SettingsStore settingsStore;
	final TorSummaryProvider torSummaryProvider;
	final ConnectionsManager connectionsManager;
	final NotificationsManager notificationsManager;
	private volatile Settings settings;
	private final MutableLiveData<OwnIdentityInfo> ownIdentityInfo =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> screenLockEnabled =
			new MutableLiveData<>();
	private final MutableLiveData<String> screenLockTimeout =
			new MutableLiveData<>();
	@Inject
	SettingsViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			SettingsManager settingsManager,
			IdentityManager identityManager,
			EventBus eventBus,
			AvatarManager avatarManager,
			AuthorManager authorManager,
			ImageCompressor imageCompressor,
			LocationUtils locationUtils,
			CircumventionProvider circumventionProvider,
			@IoExecutor Executor ioExecutor,
			FeatureFlags featureFlags) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.settingsManager = settingsManager;
		this.identityManager = identityManager;
		this.eventBus = eventBus;
		this.imageCompressor = imageCompressor;
		this.avatarManager = avatarManager;
		this.authorManager = authorManager;
		this.ioExecutor = ioExecutor;
		this.featureFlags = featureFlags;
		settingsStore = new SettingsStore(settingsManager, dbExecutor,
				SETTINGS_NAMESPACE);
		torSummaryProvider = new TorSummaryProvider(getApplication(),
				locationUtils, circumventionProvider);
		connectionsManager =
				new ConnectionsManager(settingsManager, dbExecutor);
		notificationsManager = new NotificationsManager(getApplication(),
				settingsManager, dbExecutor);
		eventBus.addListener(this);
		loadSettings();
		if (shouldEnableProfilePictures()) loadOwnIdentityInfo();
	}
	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}
	private void loadSettings() {
		runOnDbThread(() -> {
			try {
				long start = now();
				settings = settingsManager.getSettings(SETTINGS_NAMESPACE);
				updateSettings(settings);
				connectionsManager.updateBtSetting(
						settingsManager.getSettings(BT_NAMESPACE));
				connectionsManager.updateWifiSettings(
						settingsManager.getSettings(WIFI_NAMESPACE));
				connectionsManager.updateTorSettings(
						settingsManager.getSettings(TOR_NAMESPACE));
				logDuration(LOG, "Loading settings", start);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	boolean shouldEnableProfilePictures() {
		return featureFlags.shouldEnableProfilePictures();
	}
	private void loadOwnIdentityInfo() {
		runOnDbThread(() -> {
			try {
				LocalAuthor localAuthor = identityManager.getLocalAuthor();
				AuthorInfo authorInfo = authorManager.getMyAuthorInfo();
				ownIdentityInfo.postValue(
						new OwnIdentityInfo(localAuthor, authorInfo));
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof SettingsUpdatedEvent) {
			SettingsUpdatedEvent s = (SettingsUpdatedEvent) e;
			String namespace = s.getNamespace();
			if (namespace.equals(SETTINGS_NAMESPACE)) {
				LOG.info("Settings updated");
				settings = s.getSettings();
				updateSettings(settings);
			} else if (namespace.equals(BT_NAMESPACE)) {
				LOG.info("Bluetooth settings updated");
				connectionsManager.updateBtSetting(s.getSettings());
			} else if (namespace.equals(WIFI_NAMESPACE)) {
				LOG.info("Wifi settings updated");
				connectionsManager.updateWifiSettings(s.getSettings());
			} else if (namespace.equals(TOR_NAMESPACE)) {
				LOG.info("Tor settings updated");
				connectionsManager.updateTorSettings(s.getSettings());
			}
		}
	}
	@AnyThread
	private void updateSettings(Settings settings) {
		screenLockEnabled.postValue(settings.getBoolean(PREF_SCREEN_LOCK,
				false));
		int defaultTimeout = Integer.parseInt(getApplication()
				.getString(R.string.pref_lock_timeout_value_default));
		screenLockTimeout.postValue(String.valueOf(
				settings.getInt(PREF_SCREEN_LOCK_TIMEOUT, defaultTimeout)
		));
		notificationsManager.updateSettings(settings);
	}
	void setAvatar(Uri uri) {
		ioExecutor.execute(() -> {
			try {
				trySetAvatar(uri);
			} catch (IOException e) {
				logException(LOG, WARNING, e);
				onSetAvatarFailed();
			}
		});
	}
	private void trySetAvatar(Uri uri) throws IOException {
		ContentResolver contentResolver =
				getApplication().getContentResolver();
		String contentType = contentResolver.getType(uri);
		if (contentType == null) throw new IOException("null content type");
		if (!asList(getSupportedImageContentTypes()).contains(contentType)) {
			throw new UnsupportedMimeTypeException(contentType, uri);
		}
		InputStream is;
		try {
			is = contentResolver.openInputStream(uri);
			if (is == null) throw new IOException(
					"ContentResolver returned null when opening InputStream");
		} catch (SecurityException e) {
			throw new IOException(e);
		}
		InputStream compressed = imageCompressor.compressImage(is, contentType);
		runOnDbThread(() -> {
			try {
				avatarManager.addAvatar(ImageCompressor.MIME_TYPE, compressed);
				loadOwnIdentityInfo();
			} catch (IOException | DbException e) {
				logException(LOG, WARNING, e);
				onSetAvatarFailed();
			}
		});
	}
	@AnyThread
	private void onSetAvatarFailed() {
		androidExecutor.runOnUiThread(() -> Toast.makeText(getApplication(),
				R.string.change_profile_picture_failed_message, LENGTH_LONG)
				.show());
	}
	LiveData<OwnIdentityInfo> getOwnIdentityInfo() {
		return ownIdentityInfo;
	}
	LiveData<Boolean> getScreenLockEnabled() {
		return screenLockEnabled;
	}
	LiveData<String> getScreenLockTimeout() {
		return screenLockTimeout;
	}
}