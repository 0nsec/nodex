package org.nodex.android.conversation;
import android.app.Application;
import android.net.Uri;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.NoSuchContactException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.settings.Settings;
import org.nodex.core.api.settings.SettingsManager;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.Message;
import org.nodex.core.api.sync.MessageId;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.android.attachment.AttachmentCreator;
import org.nodex.android.attachment.AttachmentManager;
import org.nodex.android.attachment.AttachmentResult;
import org.nodex.android.attachment.AttachmentRetriever;
import org.nodex.android.contact.ContactItem;
import org.nodex.android.util.UiUtils;
import org.nodex.android.view.TextSendController.SendState;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.android.viewmodel.LiveEvent;
import org.nodex.android.viewmodel.MutableLiveEvent;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.autodelete.AutoDeleteManager;
import org.nodex.api.autodelete.UnexpectedTimerException;
import org.nodex.api.autodelete.event.AutoDeleteTimerMirroredEvent;
import org.nodex.api.avatar.event.AvatarUpdatedEvent;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorManager;
import org.nodex.api.messaging.MessagingManager;
import org.nodex.api.messaging.PrivateMessage;
import org.nodex.api.messaging.PrivateMessageFactory;
import org.nodex.api.messaging.PrivateMessageFormat;
import org.nodex.api.messaging.PrivateMessageHeader;
import org.nodex.api.messaging.event.AttachmentReceivedEvent;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static androidx.lifecycle.Transformations.map;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logDuration;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.core.util.LogUtils.now;
import static org.nodex.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
import static org.nodex.android.util.UiUtils.observeForeverOnce;
import static org.nodex.android.view.TextSendController.SendState.ERROR;
import static org.nodex.android.view.TextSendController.SendState.SENT;
import static org.nodex.android.view.TextSendController.SendState.UNEXPECTED_TIMER;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.api.autodelete.AutoDeleteManager.DEFAULT_TIMER_DURATION;
import static org.nodex.api.messaging.PrivateMessageFormat.TEXT_IMAGES;
import static org.nodex.api.messaging.PrivateMessageFormat.TEXT_ONLY;
@NotNullByDefault
public class ConversationViewModel extends DbViewModel
		implements EventListener, AttachmentManager {
	private static final Logger LOG =
			getLogger(ConversationViewModel.class.getName());
	private static final String SHOW_ONBOARDING_IMAGE =
			"showOnboardingImage";
	private static final String SHOW_ONBOARDING_INTRODUCTION =
			"showOnboardingIntroduction";
	private final TransactionManager db;
	private final EventBus eventBus;
	private final MessagingManager messagingManager;
	private final ContactManager contactManager;
	private final AuthorManager authorManager;
	private final SettingsManager settingsManager;
	private final PrivateMessageFactory privateMessageFactory;
	private final AttachmentRetriever attachmentRetriever;
	private final AttachmentCreator attachmentCreator;
	private final AutoDeleteManager autoDeleteManager;
	private final ConversationManager conversationManager;
	@Nullable
	private ContactId contactId = null;
	private final MutableLiveData<ContactItem> contactItem =
			new MutableLiveData<>();
	private final LiveData<String> contactName = map(contactItem, c ->
			UiUtils.getContactDisplayName(c.getContact()));
	private final LiveData<GroupId> messagingGroupId;
	private final MutableLiveData<PrivateMessageFormat> privateMessageFormat =
			new MutableLiveData<>();
	private final MutableLiveEvent<Boolean> showImageOnboarding =
			new MutableLiveEvent<>();
	private final MutableLiveEvent<Boolean> showIntroductionOnboarding =
			new MutableLiveEvent<>();
	private final MutableLiveData<Boolean> showIntroductionAction =
			new MutableLiveData<>();
	private final MutableLiveData<Long> autoDeleteTimer =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> contactDeleted =
			new MutableLiveData<>(false);
	private final MutableLiveEvent<PrivateMessageHeader> addedHeader =
			new MutableLiveEvent<>();
	@Inject
	ConversationViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			EventBus eventBus,
			MessagingManager messagingManager,
			ContactManager contactManager,
			AuthorManager authorManager,
			SettingsManager settingsManager,
			PrivateMessageFactory privateMessageFactory,
			AttachmentRetriever attachmentRetriever,
			AttachmentCreator attachmentCreator,
			AutoDeleteManager autoDeleteManager,
			ConversationManager conversationManager) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.db = db;
		this.eventBus = eventBus;
		this.messagingManager = messagingManager;
		this.contactManager = contactManager;
		this.authorManager = authorManager;
		this.settingsManager = settingsManager;
		this.privateMessageFactory = privateMessageFactory;
		this.attachmentRetriever = attachmentRetriever;
		this.attachmentCreator = attachmentCreator;
		this.autoDeleteManager = autoDeleteManager;
		this.conversationManager = conversationManager;
		messagingGroupId = map(contactItem, c ->
				messagingManager.getContactGroup(c.getContact()).getId());
		eventBus.addListener(this);
	}
	@Override
	protected void onCleared() {
		super.onCleared();
		attachmentCreator.cancel();
		eventBus.removeListener(this);
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof AttachmentReceivedEvent) {
			AttachmentReceivedEvent a = (AttachmentReceivedEvent) e;
			if (a.getContactId().equals(contactId)) {
				LOG.info("Attachment received");
				runOnDbThread(() -> attachmentRetriever
						.loadAttachmentItem(a.getMessageId()));
			}
		} else if (e instanceof AutoDeleteTimerMirroredEvent) {
			AutoDeleteTimerMirroredEvent a = (AutoDeleteTimerMirroredEvent) e;
			if (a.getContactId().equals(contactId)) {
				autoDeleteTimer.setValue(a.getNewTimer());
			}
		} else if (e instanceof AvatarUpdatedEvent) {
			AvatarUpdatedEvent a = (AvatarUpdatedEvent) e;
			if (a.getContactId().equals(contactId)) {
				LOG.info("Avatar updated");
				updateAvatar(a);
			}
		}
	}
	@UiThread
	private void updateAvatar(AvatarUpdatedEvent a) {
		observeForeverOnce(contactItem, oldContactItem -> {
			requireNonNull(oldContactItem);
			AuthorInfo oldAuthorInfo = oldContactItem.getAuthorInfo();
			AuthorInfo newAuthorInfo = new AuthorInfo(oldAuthorInfo.getStatus(),
					oldAuthorInfo.getAlias(), a.getAttachmentHeader());
			ContactItem newContactItem =
					new ContactItem(oldContactItem.getContact(), newAuthorInfo);
			contactItem.setValue(newContactItem);
		});
	}
	void setContactId(ContactId contactId) {
		if (this.contactId == null) {
			this.contactId = contactId;
			loadContact(contactId);
		} else if (!contactId.equals(this.contactId)) {
			throw new IllegalStateException();
		}
	}
	private void loadContact(ContactId contactId) {
		runOnDbThread(() -> {
			try {
				long start = now();
				Contact c = contactManager.getContact(contactId);
				AuthorInfo authorInfo = authorManager.getAuthorInfo(c);
				contactItem.postValue(new ContactItem(c, authorInfo));
				logDuration(LOG, "Loading contact", start);
				start = now();
				long timer = db.transactionWithResult(true, txn ->
						autoDeleteManager.getAutoDeleteTimer(txn, contactId));
				autoDeleteTimer.postValue(timer);
				logDuration(LOG, "Getting auto-delete timer", start);
				start = now();
				checkFeaturesAndOnboarding(contactId);
				logDuration(LOG, "Checking for image support", start);
			} catch (NoSuchContactException e) {
				contactDeleted.postValue(true);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	void markMessageRead(GroupId g, MessageId m) {
		runOnDbThread(() -> {
			try {
				long start = now();
				conversationManager.setReadFlag(g, m, true);
				logDuration(LOG, "Marking read", start);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	void setContactAlias(String alias) {
		runOnDbThread(() -> {
			try {
				contactManager.setContactAlias(requireNonNull(contactId),
						alias.isEmpty() ? null : alias);
				loadContact(contactId);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	@Override
	@UiThread
	public LiveData<AttachmentResult> storeAttachments(Collection<Uri> uris,
			boolean restart) {
		if (restart) {
			return attachmentCreator.getLiveAttachments();
		} else {
			return attachmentCreator.storeAttachments(messagingGroupId, uris);
		}
	}
	@Override
	@UiThread
	public List<AttachmentHeader> getAttachmentHeadersForSending() {
		return attachmentCreator.getAttachmentHeadersForSending();
	}
	@Override
	@UiThread
	public void cancel() {
		attachmentCreator.cancel();
	}
	@DatabaseExecutor
	private void checkFeaturesAndOnboarding(ContactId c) throws DbException {
		PrivateMessageFormat format = db.transactionWithResult(true, txn ->
				messagingManager.getContactMessageFormat(txn, c));
		if (LOG.isLoggable(INFO))
			LOG.info("PrivateMessageFormat loaded: " + format.name());
		privateMessageFormat.postValue(format);
		Collection<Contact> contacts = contactManager.getContacts();
		boolean introductionSupported = contacts.size() > 1;
		showIntroductionAction.postValue(introductionSupported);
		Settings settings = settingsManager.getSettings(SETTINGS_NAMESPACE);
		if (format != TEXT_ONLY &&
				settings.getBoolean(SHOW_ONBOARDING_IMAGE, true)) {
			onOnboardingShown(SHOW_ONBOARDING_IMAGE);
			showImageOnboarding.postEvent(true);
		} else if (introductionSupported &&
				settings.getBoolean(SHOW_ONBOARDING_INTRODUCTION, true)) {
			onOnboardingShown(SHOW_ONBOARDING_INTRODUCTION);
			showIntroductionOnboarding.postEvent(true);
		}
	}
	@DatabaseExecutor
	private void onOnboardingShown(String key) throws DbException {
		Settings settings = new Settings();
		settings.putBoolean(key, false);
		settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
	}
	@UiThread
	LiveData<SendState> sendMessage(@Nullable String text,
			List<AttachmentHeader> headers, long expectedTimer) {
		MutableLiveData<SendState> liveData = new MutableLiveData<>();
		runOnDbThread(() -> {
			try {
				db.transaction(false, txn -> {
					long start = now();
					PrivateMessage m = createMessage(txn, text, headers,
							expectedTimer);
					messagingManager.addLocalMessage(txn, m);
					logDuration(LOG, "Storing message", start);
					Message message = m.getMessage();
					PrivateMessageHeader h = new PrivateMessageHeader(
							message.getId(), message.getGroupId(),
							message.getTimestamp(), true, true, false, false,
							m.hasText(), m.getAttachmentHeaders(),
							m.getAutoDeleteTimer());
					MessageId id = message.getId();
					txn.attach(() -> {
						attachmentCreator.onAttachmentsSent(id);
						liveData.setValue(SENT);
						addedHeader.setEvent(h);
					});
				});
			} catch (UnexpectedTimerException e) {
				liveData.postValue(UNEXPECTED_TIMER);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				liveData.postValue(ERROR);
			}
		});
		return liveData;
	}
	private PrivateMessage createMessage(Transaction txn, @Nullable String text,
			List<AttachmentHeader> headers, long expectedTimer)
			throws DbException {
		Contact contact = requireNonNull(contactItem.getValue()).getContact();
		GroupId groupId = messagingManager.getContactGroup(contact).getId();
		PrivateMessageFormat format =
				requireNonNull(privateMessageFormat.getValue());
		long timestamp = conversationManager
				.getTimestampForOutgoingMessage(txn, requireNonNull(contactId));
		try {
			if (format == TEXT_ONLY) {
				return privateMessageFactory.createLegacyPrivateMessage(
						groupId, timestamp, requireNonNull(text));
			} else if (format == TEXT_IMAGES) {
				return privateMessageFactory.createPrivateMessage(groupId,
						timestamp, text, headers);
			} else {
				long timer = autoDeleteManager
						.getAutoDeleteTimer(txn, contactId, timestamp);
				if (timer != expectedTimer)
					throw new UnexpectedTimerException();
				return privateMessageFactory.createPrivateMessage(groupId,
						timestamp, text, headers, timer);
			}
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
	void setAutoDeleteTimerEnabled(boolean enabled) {
		long timer = enabled ? DEFAULT_TIMER_DURATION : NO_AUTO_DELETE_TIMER;
		final ContactId c = requireNonNull(contactId);
		runOnDbThread(() -> {
			try {
				db.transaction(false, txn ->
						autoDeleteManager.setAutoDeleteTimer(txn, c, timer));
				autoDeleteTimer.postValue(timer);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	AttachmentRetriever getAttachmentRetriever() {
		return attachmentRetriever;
	}
	LiveData<ContactItem> getContactItem() {
		return contactItem;
	}
	LiveData<String> getContactDisplayName() {
		return contactName;
	}
	LiveData<PrivateMessageFormat> getPrivateMessageFormat() {
		return privateMessageFormat;
	}
	LiveEvent<Boolean> showImageOnboarding() {
		return showImageOnboarding;
	}
	LiveEvent<Boolean> showIntroductionOnboarding() {
		return showIntroductionOnboarding;
	}
	LiveData<Boolean> showIntroductionAction() {
		return showIntroductionAction;
	}
	LiveData<Long> getAutoDeleteTimer() {
		return autoDeleteTimer;
	}
	LiveData<Boolean> isContactDeleted() {
		return contactDeleted;
	}
	LiveEvent<PrivateMessageHeader> getAddedPrivateMessage() {
		return addedHeader;
	}
	@UiThread
	void recheckFeaturesAndOnboarding(ContactId contactId) {
		runOnDbThread(() -> {
			try {
				checkFeaturesAndOnboarding(contactId);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
}