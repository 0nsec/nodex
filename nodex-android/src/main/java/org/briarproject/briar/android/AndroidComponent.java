package org.nodex.android;
import org.nodex.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import org.nodex.core.BrambleAndroidEagerSingletons;
import org.nodex.core.BrambleAndroidModule;
import org.nodex.core.BrambleAppComponent;
import org.nodex.core.BrambleCoreEagerSingletons;
import org.nodex.core.BrambleCoreModule;
import org.nodex.core.account.BriarAccountModule;
import org.nodex.core.api.FeatureFlags;
import org.nodex.core.api.account.AccountManager;
import org.nodex.core.api.connection.ConnectionRegistry;
import org.nodex.core.api.contact.ContactExchangeManager;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.crypto.CryptoExecutor;
import org.nodex.core.api.crypto.PasswordStrengthEstimator;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.identity.IdentityManager;
import org.nodex.core.api.keyagreement.KeyAgreementTask;
import org.nodex.core.api.keyagreement.PayloadEncoder;
import org.nodex.core.api.keyagreement.PayloadParser;
import org.nodex.core.api.lifecycle.IoExecutor;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.plugin.PluginManager;
import org.nodex.core.api.settings.SettingsManager;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.core.api.system.Clock;
import org.nodex.core.mailbox.ModularMailboxModule;
import org.nodex.core.plugin.file.RemovableDriveModule;
import org.nodex.core.system.ClockModule;
import org.nodex.BriarCoreEagerSingletons;
import org.nodex.BriarCoreModule;
import org.nodex.android.attachment.AttachmentModule;
import org.nodex.android.attachment.media.MediaModule;
import org.nodex.android.contact.connect.BluetoothIntroFragment;
import org.nodex.android.conversation.glide.BriarModelLoader;
import org.nodex.android.hotspot.AbstractTabsFragment;
import org.nodex.android.hotspot.FallbackFragment;
import org.nodex.android.hotspot.HotspotIntroFragment;
import org.nodex.android.hotspot.ManualHotspotFragment;
import org.nodex.android.hotspot.QrHotspotFragment;
import org.nodex.android.logging.CachingLogHandler;
import org.nodex.android.login.SignInReminderReceiver;
import org.nodex.android.mailbox.ErrorFragment;
import org.nodex.android.mailbox.ErrorWizardFragment;
import org.nodex.android.mailbox.MailboxScanFragment;
import org.nodex.android.mailbox.MailboxStatusFragment;
import org.nodex.android.mailbox.OfflineFragment;
import org.nodex.android.mailbox.SetupDownloadFragment;
import org.nodex.android.mailbox.SetupIntroFragment;
import org.nodex.android.removabledrive.ChooserFragment;
import org.nodex.android.removabledrive.ReceiveFragment;
import org.nodex.android.removabledrive.SendFragment;
import org.nodex.android.settings.ConnectionsFragment;
import org.nodex.android.settings.NotificationsFragment;
import org.nodex.android.settings.SecurityFragment;
import org.nodex.android.settings.SettingsFragment;
import org.nodex.android.view.EmojiTextInputView;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.android.DozeWatchdog;
import org.nodex.api.android.LockManager;
import org.nodex.api.android.ScreenFilterMonitor;
import org.nodex.api.attachment.AttachmentReader;
import org.nodex.api.autodelete.AutoDeleteManager;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogPostFactory;
import org.nodex.api.blog.BlogSharingManager;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.feed.FeedManager;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.api.identity.AuthorManager;
import org.nodex.api.introduction.IntroductionManager;
import org.nodex.api.messaging.MessagingManager;
import org.nodex.api.messaging.PrivateMessageFactory;
import org.nodex.api.privategroup.GroupMessageFactory;
import org.nodex.api.privategroup.PrivateGroupFactory;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.invitation.GroupInvitationFactory;
import org.nodex.api.privategroup.invitation.GroupInvitationManager;
import org.nodex.api.test.TestDataCreator;
import org.nodex.onionwrapper.CircumventionProvider;
import org.nodex.onionwrapper.LocationUtils;
import java.util.concurrent.Executor;
import javax.inject.Singleton;
import androidx.lifecycle.ViewModelProvider;
import dagger.Component;
@Singleton
@Component(modules = {
		BrambleCoreModule.class,
		BriarCoreModule.class,
		BrambleAndroidModule.class,
		BriarAccountModule.class,
		AppModule.class,
		AttachmentModule.class,
		ClockModule.class,
		MediaModule.class,
		ModularMailboxModule.class,
		RemovableDriveModule.class
})
public interface AndroidComponent
		extends BrambleCoreEagerSingletons, BrambleAndroidEagerSingletons,
		BriarCoreEagerSingletons, AndroidEagerSingletons, BrambleAppComponent {
	@CryptoExecutor
	Executor cryptoExecutor();
	PasswordStrengthEstimator passwordStrengthIndicator();
	@DatabaseExecutor
	Executor databaseExecutor();
	TransactionManager transactionManager();
	MessageTracker messageTracker();
	LifecycleManager lifecycleManager();
	IdentityManager identityManager();
	AttachmentReader attachmentReader();
	AuthorManager authorManager();
	PluginManager pluginManager();
	EventBus eventBus();
	AndroidNotificationManager androidNotificationManager();
	ScreenFilterMonitor screenFilterMonitor();
	ConnectionRegistry connectionRegistry();
	ContactManager contactManager();
	ConversationManager conversationManager();
	MessagingManager messagingManager();
	PrivateMessageFactory privateMessageFactory();
	PrivateGroupManager privateGroupManager();
	GroupInvitationFactory groupInvitationFactory();
	GroupInvitationManager groupInvitationManager();
	PrivateGroupFactory privateGroupFactory();
	GroupMessageFactory groupMessageFactory();
	ForumManager forumManager();
	ForumSharingManager forumSharingManager();
	BlogSharingManager blogSharingManager();
	BlogManager blogManager();
	BlogPostFactory blogPostFactory();
	SettingsManager settingsManager();
	ContactExchangeManager contactExchangeManager();
	KeyAgreementTask keyAgreementTask();
	PayloadEncoder payloadEncoder();
	PayloadParser payloadParser();
	IntroductionManager introductionManager();
	AndroidExecutor androidExecutor();
	FeedManager feedManager();
	Clock clock();
	TestDataCreator testDataCreator();
	DozeWatchdog dozeWatchdog();
	@IoExecutor
	Executor ioExecutor();
	AccountManager accountManager();
	LockManager lockManager();
	LocationUtils locationUtils();
	CircumventionProvider circumventionProvider();
	ViewModelProvider.Factory viewModelFactory();
	FeatureFlags featureFlags();
	AndroidWakeLockManager wakeLockManager();
	CachingLogHandler logHandler();
	Thread.UncaughtExceptionHandler exceptionHandler();
	AutoDeleteManager autoDeleteManager();
	void inject(SignInReminderReceiver briarService);
	void inject(BriarService briarService);
	void inject(NotificationCleanupService notificationCleanupService);
	void inject(EmojiTextInputView textInputView);
	void inject(BriarModelLoader briarModelLoader);
	void inject(SettingsFragment settingsFragment);
	void inject(ConnectionsFragment connectionsFragment);
	void inject(SecurityFragment securityFragment);
	void inject(NotificationsFragment notificationsFragment);
	void inject(HotspotIntroFragment hotspotIntroFragment);
	void inject(AbstractTabsFragment abstractTabsFragment);
	void inject(QrHotspotFragment qrHotspotFragment);
	void inject(ManualHotspotFragment manualHotspotFragment);
	void inject(FallbackFragment fallbackFragment);
	void inject(ChooserFragment chooserFragment);
	void inject(SendFragment sendFragment);
	void inject(ReceiveFragment receiveFragment);
	void inject(BluetoothIntroFragment bluetoothIntroFragment);
	void inject(SetupIntroFragment setupIntroFragment);
	void inject(SetupDownloadFragment setupDownloadFragment);
	void inject(MailboxScanFragment mailboxScanFragment);
	void inject(OfflineFragment offlineFragment);
	void inject(ErrorFragment errorFragment);
	void inject(MailboxStatusFragment mailboxStatusFragment);
	void inject(ErrorWizardFragment errorWizardFragment);
}