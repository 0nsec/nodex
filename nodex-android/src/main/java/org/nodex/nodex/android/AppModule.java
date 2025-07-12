package org.nodex.android;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import com.vanniktech.emoji.RecentEmoji;
import org.nodex.core.api.FeatureFlags;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.crypto.CryptoComponent;
import org.nodex.core.api.crypto.KeyStrengthener;
import org.nodex.core.api.crypto.PublicKey;
import org.nodex.core.api.db.DatabaseConfig;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.mailbox.MailboxDirectory;
import org.nodex.core.api.plugin.BluetoothConstants;
import org.nodex.core.api.plugin.LanTcpConstants;
import org.nodex.core.api.plugin.PluginConfig;
import org.nodex.core.api.plugin.TorControlPort;
import org.nodex.core.api.plugin.TorDirectory;
import org.nodex.core.api.plugin.TorSocksPort;
import org.nodex.core.api.plugin.TransportId;
import org.nodex.core.api.plugin.duplex.DuplexPluginFactory;
import org.nodex.core.api.plugin.simplex.SimplexPluginFactory;
import org.nodex.core.api.reporting.DevConfig;
import org.nodex.core.plugin.bluetooth.AndroidBluetoothPluginFactory;
import org.nodex.core.plugin.file.AndroidRemovableDrivePluginFactory;
import org.nodex.core.plugin.file.MailboxPluginFactory;
import org.nodex.core.plugin.tcp.AndroidLanTcpPluginFactory;
import org.nodex.core.plugin.tor.AndroidTorPluginFactory;
import org.nodex.core.util.AndroidUtils;
import org.nodex.core.util.StringUtils;
import org.nodex.android.account.DozeHelperModule;
import org.nodex.android.account.LockManagerImpl;
import org.nodex.android.account.SetupModule;
import org.nodex.android.blog.BlogModule;
import org.nodex.android.contact.ContactListModule;
import org.nodex.android.contact.add.nearby.AddNearbyContactModule;
import org.nodex.android.contact.connect.ConnectViaBluetoothModule;
import org.nodex.android.forum.ForumModule;
import org.nodex.android.hotspot.HotspotModule;
import org.nodex.android.introduction.IntroductionModule;
import org.nodex.android.logging.LoggingModule;
import org.nodex.android.login.LoginModule;
import org.nodex.android.mailbox.MailboxModule;
import org.nodex.android.navdrawer.NavDrawerModule;
import org.nodex.android.privategroup.conversation.GroupConversationModule;
import org.nodex.android.privategroup.list.GroupListModule;
import org.nodex.android.removabledrive.TransferDataModule;
import org.nodex.android.reporting.DevReportModule;
import org.nodex.android.settings.SettingsModule;
import org.nodex.android.sharing.SharingModule;
import org.nodex.android.test.TestAvatarCreatorImpl;
import org.nodex.android.viewmodel.ViewModelModule;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.android.DozeWatchdog;
import org.nodex.api.android.LockManager;
import org.nodex.api.android.NetworkUsageMetrics;
import org.nodex.api.android.ScreenFilterMonitor;
import org.nodex.api.test.TestAvatarCreator;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.File;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import static android.content.Context.MODE_PRIVATE;
import static android.os.Build.VERSION.SDK_INT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.nodex.core.api.plugin.TorConstants.DEFAULT_CONTROL_PORT;
import static org.nodex.core.api.plugin.TorConstants.DEFAULT_SOCKS_PORT;
import static org.nodex.core.api.reporting.ReportingConstants.DEV_ONION_ADDRESS;
import static org.nodex.core.api.reporting.ReportingConstants.DEV_PUBLIC_KEY_HEX;
import static org.nodex.android.TestingConstants.IS_DEBUG_BUILD;
@Module(includes = {
		SetupModule.class,
		DozeHelperModule.class,
		AddNearbyContactModule.class,
		LoggingModule.class,
		LoginModule.class,
		NavDrawerModule.class,
		ViewModelModule.class,
		SettingsModule.class,
		DevReportModule.class,
		ContactListModule.class,
		IntroductionModule.class,
		ConnectViaBluetoothModule.class,
		BlogModule.class,
		ForumModule.class,
		GroupListModule.class,
		GroupConversationModule.class,
		SharingModule.class,
		HotspotModule.class,
		TransferDataModule.class,
		MailboxModule.class,
})
public class AppModule {
	static class EagerSingletons {
		@Inject
		AndroidNotificationManager androidNotificationManager;
		@Inject
		ScreenFilterMonitor screenFilterMonitor;
		@Inject
		NetworkUsageMetrics networkUsageMetrics;
		@Inject
		DozeWatchdog dozeWatchdog;
		@Inject
		LockManager lockManager;
		@Inject
		RecentEmoji recentEmoji;
	}
	private final Application application;
	public AppModule(Application application) {
		this.application = application;
	}
	public static AndroidComponent getAndroidComponent(Context ctx) {
		NodexApplication app = (NodexApplication) ctx.getApplicationContext();
		return app.getApplicationComponent();
	}
	@Provides
	@Singleton
	Application providesApplication() {
		return application;
	}
	@Provides
	@Singleton
	DatabaseConfig provideDatabaseConfig(Application app) {
		StrictMode.ThreadPolicy tp = StrictMode.allowThreadDiskReads();
		StrictMode.allowThreadDiskWrites();
		File dbDir = app.getApplicationContext().getDir("db", MODE_PRIVATE);
		File keyDir = app.getApplicationContext().getDir("key", MODE_PRIVATE);
		StrictMode.setThreadPolicy(tp);
		KeyStrengthener keyStrengthener = SDK_INT >= 23
				? new AndroidKeyStrengthener() : null;
		return new AndroidDatabaseConfig(dbDir, keyDir, keyStrengthener);
	}
	@Provides
	@Singleton
	@MailboxDirectory
	File provideMailboxDirectory(Application app) {
		return app.getDir("mailbox", MODE_PRIVATE);
	}
	@Provides
	@Singleton
	@TorDirectory
	File provideTorDirectory(Application app) {
		return app.getDir("tor", MODE_PRIVATE);
	}
	@Provides
	@Singleton
	@TorSocksPort
	int provideTorSocksPort() {
		if (!IS_DEBUG_BUILD) {
			return DEFAULT_SOCKS_PORT;
		} else {
			return DEFAULT_SOCKS_PORT + 2;
		}
	}
	@Provides
	@Singleton
	@TorControlPort
	int provideTorControlPort() {
		if (!IS_DEBUG_BUILD) {
			return DEFAULT_CONTROL_PORT;
		} else {
			return DEFAULT_CONTROL_PORT + 2;
		}
	}
	@Provides
	@Singleton
	PluginConfig providePluginConfig(AndroidBluetoothPluginFactory bluetooth,
			AndroidTorPluginFactory tor, AndroidLanTcpPluginFactory lan,
			AndroidRemovableDrivePluginFactory drive,
			MailboxPluginFactory mailbox, FeatureFlags featureFlags) {
		@NotNullByDefault
		PluginConfig pluginConfig = new PluginConfig() {
			@Override
			public Collection<DuplexPluginFactory> getDuplexFactories() {
				return asList(bluetooth, tor, lan);
			}
			@Override
			public Collection<SimplexPluginFactory> getSimplexFactories() {
				List<SimplexPluginFactory> simplex = new ArrayList<>();
				simplex.add(mailbox);
				simplex.add(drive);
				return simplex;
			}
			@Override
			public boolean shouldPoll() {
				return true;
			}
			@Override
			public Map<TransportId, List<TransportId>> getTransportPreferences() {
				return singletonMap(BluetoothConstants.ID,
						singletonList(LanTcpConstants.ID));
			}
		};
		return pluginConfig;
	}
	@Provides
	@Singleton
	DevConfig provideDevConfig(Application app, CryptoComponent crypto) {
		@NotNullByDefault
		DevConfig devConfig = new DevConfig() {
			@Override
			public PublicKey getDevPublicKey() {
				try {
					return crypto.getMessageKeyParser().parsePublicKey(
							StringUtils.fromHexString(DEV_PUBLIC_KEY_HEX));
				} catch (GeneralSecurityException | FormatException e) {
					throw new RuntimeException(e);
				}
			}
			@Override
			public String getDevOnionAddress() {
				return DEV_ONION_ADDRESS;
			}
			@Override
			public File getReportDir() {
				return AndroidUtils.getReportDir(app.getApplicationContext());
			}
			@Override
			public File getLogcatFile() {
				return AndroidUtils.getLogcatFile(app.getApplicationContext());
			}
		};
		return devConfig;
	}
	@Provides
	TestAvatarCreator provideTestAvatarCreator(
			TestAvatarCreatorImpl testAvatarCreator) {
		return testAvatarCreator;
	}
	@Provides
	SharedPreferences provideSharedPreferences(Application app) {
		return app.getSharedPreferences("db", MODE_PRIVATE);
	}
	@Provides
	@Singleton
	AndroidNotificationManager provideAndroidNotificationManager(
			LifecycleManager lifecycleManager, EventBus eventBus,
			AndroidNotificationManagerImpl notificationManager) {
		lifecycleManager.registerService(notificationManager);
		eventBus.addListener(notificationManager);
		return notificationManager;
	}
	@Provides
	@Singleton
	ScreenFilterMonitor provideScreenFilterMonitor(
			LifecycleManager lifecycleManager,
			ScreenFilterMonitorImpl screenFilterMonitor) {
		if (SDK_INT <= 29) {
			lifecycleManager.registerService(screenFilterMonitor);
		}
		return screenFilterMonitor;
	}
	@Provides
	@Singleton
	NetworkUsageMetrics provideNetworkUsageMetrics(
			LifecycleManager lifecycleManager) {
		NetworkUsageMetrics networkUsageMetrics = new NetworkUsageMetricsImpl();
		lifecycleManager.registerService(networkUsageMetrics);
		return networkUsageMetrics;
	}
	@Provides
	@Singleton
	DozeWatchdog provideDozeWatchdog(LifecycleManager lifecycleManager) {
		DozeWatchdogImpl dozeWatchdog = new DozeWatchdogImpl(application);
		lifecycleManager.registerService(dozeWatchdog);
		return dozeWatchdog;
	}
	@Provides
	@Singleton
	LockManager provideLockManager(LifecycleManager lifecycleManager,
			EventBus eventBus, LockManagerImpl lockManager) {
		lifecycleManager.registerService(lockManager);
		eventBus.addListener(lockManager);
		return lockManager;
	}
	@Provides
	@Singleton
	RecentEmoji provideRecentEmoji(LifecycleManager lifecycleManager,
			RecentEmojiImpl recentEmoji) {
		lifecycleManager.registerOpenDatabaseHook(recentEmoji);
		return recentEmoji;
	}
	@Provides
	FeatureFlags provideFeatureFlags() {
		return new FeatureFlags() {
			@Override
			public boolean shouldEnableImageAttachments() {
				return true;
			}
			@Override
			public boolean shouldEnableProfilePictures() {
				return true;
			}
			@Override
			public boolean shouldEnableDisappearingMessages() {
				return true;
			}
			@Override
			public boolean shouldEnablePrivateGroupsInCore() {
				return true;
			}
			@Override
			public boolean shouldEnableForumsInCore() {
				return true;
			}
			@Override
			public boolean shouldEnableBlogsInCore() {
				return true;
			}
		};
	}
}