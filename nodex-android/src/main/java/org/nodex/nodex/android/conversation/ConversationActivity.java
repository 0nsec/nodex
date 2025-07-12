package org.nodex.android.conversation;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.transition.Slide;
import android.transition.Transition;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import org.nodex.core.api.FeatureFlags;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.Pair;
import org.nodex.core.api.connection.ConnectionRegistry;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.contact.event.ContactRemovedEvent;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.NoSuchContactException;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.plugin.event.ContactConnectedEvent;
import org.nodex.core.api.plugin.event.ContactDisconnectedEvent;
import org.nodex.core.api.sync.ClientId;
import org.nodex.core.api.sync.MessageId;
import org.nodex.core.api.sync.event.MessagesAckedEvent;
import org.nodex.core.api.sync.event.MessagesSentEvent;
import org.nodex.core.api.versioning.event.ClientVersionUpdatedEvent;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.BriarActivity;
import org.nodex.android.attachment.AttachmentItem;
import org.nodex.android.attachment.AttachmentRetriever;
import org.nodex.android.blog.BlogActivity;
import org.nodex.android.contact.connect.ConnectViaBluetoothActivity;
import org.nodex.android.conversation.ConversationVisitor.AttachmentCache;
import org.nodex.android.conversation.ConversationVisitor.TextCache;
import org.nodex.android.forum.ForumActivity;
import org.nodex.android.fragment.BaseFragment.BaseFragmentListener;
import org.nodex.android.introduction.IntroductionActivity;
import org.nodex.android.privategroup.conversation.GroupActivity;
import org.nodex.android.removabledrive.RemovableDriveActivity;
import org.nodex.android.util.ActivityLaunchers.GetMultipleImagesAdvanced;
import org.nodex.android.util.ActivityLaunchers.OpenMultipleImageDocumentsAdvanced;
import org.nodex.android.util.BriarSnackbarBuilder;
import org.nodex.android.view.BriarRecyclerView;
import org.nodex.android.view.ImagePreview;
import org.nodex.android.view.TextAttachmentController;
import org.nodex.android.view.TextAttachmentController.AttachmentListener;
import org.nodex.android.view.TextInputView;
import org.nodex.android.view.TextSendController;
import org.nodex.android.view.TextSendController.SendState;
import org.nodex.android.widget.LinkDialogFragment;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.autodelete.event.ConversationMessagesDeletedEvent;
import org.nodex.api.blog.BlogSharingManager;
import org.nodex.api.client.ProtocolStateException;
import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.conversation.ConversationMessageHeader;
import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.conversation.ConversationRequest;
import org.nodex.api.conversation.ConversationResponse;
import org.nodex.api.conversation.DeletionResult;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.api.introduction.IntroductionManager;
import org.nodex.api.messaging.MessagingManager;
import org.nodex.api.messaging.PrivateMessageHeader;
import org.nodex.api.privategroup.invitation.GroupInvitationManager;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.SelectionTracker.SelectionObserver;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import static android.view.Gravity.RIGHT;
import static android.widget.Toast.LENGTH_SHORT;
import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static androidx.recyclerview.widget.SortedList.INVALID_POSITION;
import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logDuration;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.core.util.LogUtils.now;
import static org.nodex.core.util.StringUtils.fromHexString;
import static org.nodex.core.util.StringUtils.isNullOrEmpty;
import static org.nodex.core.util.StringUtils.join;
import static org.nodex.android.activity.RequestCodes.REQUEST_INTRODUCTION;
import static org.nodex.android.conversation.ImageActivity.ATTACHMENTS;
import static org.nodex.android.conversation.ImageActivity.ATTACHMENT_POSITION;
import static org.nodex.android.conversation.ImageActivity.DATE;
import static org.nodex.android.conversation.ImageActivity.ITEM_ID;
import static org.nodex.android.conversation.ImageActivity.NAME;
import static org.nodex.android.util.UiUtils.launchActivityToOpenFile;
import static org.nodex.android.util.UiUtils.observeOnce;
import static org.nodex.android.view.AuthorView.setAvatar;
import static org.nodex.api.messaging.MessagingConstants.MAX_ATTACHMENTS_PER_MESSAGE;
import static org.nodex.api.messaging.MessagingConstants.MAX_PRIVATE_MESSAGE_TEXT_LENGTH;
import static org.nodex.api.messaging.PrivateMessageFormat.TEXT_IMAGES_AUTO_DELETE;
import static org.nodex.api.messaging.PrivateMessageFormat.TEXT_ONLY;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ConversationActivity extends BriarActivity
		implements BaseFragmentListener, EventListener, ConversationListener,
		TextCache, AttachmentCache, AttachmentListener, ActionMode.Callback {
	public static final String CONTACT_ID = "nodex.CONTACT_ID";
	private static final Logger LOG =
			getLogger(ConversationActivity.class.getName());
	private static final int TRANSITION_DURATION_MS = 500;
	private static final int ONBOARDING_DELAY_MS = 250;
	@Inject
	AndroidNotificationManager notificationManager;
	@Inject
	ConnectionRegistry connectionRegistry;
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	@Inject
	FeatureFlags featureFlags;
	@Inject
	volatile ContactManager contactManager;
	@Inject
	volatile MessagingManager messagingManager;
	@Inject
	volatile ConversationManager conversationManager;
	@Inject
	volatile EventBus eventBus;
	@Inject
	volatile IntroductionManager introductionManager;
	@Inject
	volatile ForumSharingManager forumSharingManager;
	@Inject
	volatile BlogSharingManager blogSharingManager;
	@Inject
	volatile GroupInvitationManager groupInvitationManager;
	private final Map<MessageId, String> textCache = new ConcurrentHashMap<>();
	private final Observer<String> contactNameObserver = name -> {
		requireNonNull(name);
		loadMessages();
	};
	private final ActivityResultLauncher<String[]> docLauncher =
			registerForActivityResult(new OpenMultipleImageDocumentsAdvanced(),
					this::onImagesChosen);
	private final ActivityResultLauncher<String> contentLauncher =
			registerForActivityResult(new GetMultipleImagesAdvanced(),
					this::onImagesChosen);
	private AttachmentRetriever attachmentRetriever;
	private ConversationViewModel viewModel;
	private ConversationVisitor visitor;
	private ConversationAdapter adapter;
	private Toolbar toolbar;
	private CircleImageView toolbarAvatar;
	private ImageView toolbarStatus;
	private TextView toolbarTitle;
	private BriarRecyclerView list;
	private LinearLayoutManager layoutManager;
	private TextInputView textInputView;
	private TextSendController sendController;
	private SelectionTracker<String> tracker;
	@Nullable
	private Parcelable layoutManagerState;
	@Nullable
	private ActionMode actionMode;
	private volatile ContactId contactId;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(ConversationViewModel.class);
	}
	@Override
	public void onCreate(@Nullable Bundle state) {
		@SuppressLint("RtlHardcoded")
		Transition slide = new Slide(RIGHT);
		slide.setDuration(TRANSITION_DURATION_MS);
		setSceneTransitionAnimation(slide, null, slide);
		super.onCreate(state);
		Intent i = getIntent();
		int id = i.getIntExtra(CONTACT_ID, -1);
		if (id == -1) throw new IllegalStateException();
		contactId = new ContactId(id);
		viewModel.setContactId(contactId);
		attachmentRetriever = viewModel.getAttachmentRetriever();
		setContentView(R.layout.activity_conversation);
		toolbar = requireNonNull(setUpCustomToolbar(true));
		toolbarAvatar = toolbar.findViewById(R.id.contactAvatar);
		toolbarStatus = toolbar.findViewById(R.id.contactStatus);
		toolbarTitle = toolbar.findViewById(R.id.contactName);
		viewModel.getContactItem().observe(this, contactItem -> {
			requireNonNull(contactItem);
			setAvatar(toolbarAvatar, contactItem);
		});
		viewModel.getContactDisplayName().observe(this, contactName -> {
			requireNonNull(contactName);
			toolbarTitle.setText(contactName);
		});
		viewModel.isContactDeleted().observe(this, deleted -> {
			requireNonNull(deleted);
			if (deleted) finish();
		});
		viewModel.getAddedPrivateMessage().observeEvent(this,
				this::onAddedPrivateMessage);
		visitor = new ConversationVisitor(this, this, this,
				viewModel.getContactDisplayName());
		adapter = new ConversationAdapter(this, this);
		list = findViewById(R.id.conversationView);
		layoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(layoutManager);
		list.setAdapter(adapter);
		list.setEmptyText(getString(R.string.no_private_messages));
		ConversationScrollListener scrollListener =
				new ConversationScrollListener(adapter, viewModel);
		list.getRecyclerView().addOnScrollListener(scrollListener);
		addSelectionTracker();
		textInputView = findViewById(R.id.text_input_container);
		if (featureFlags.shouldEnableImageAttachments()) {
			ImagePreview imagePreview = findViewById(R.id.imagePreview);
			sendController = new TextAttachmentController(textInputView,
					imagePreview, this, viewModel);
			observeOnce(viewModel.getPrivateMessageFormat(), this, format -> {
				if (format != TEXT_ONLY) {
					((TextAttachmentController) sendController)
							.setImagesSupported();
				}
			});
		} else {
			sendController = new TextSendController(textInputView, this, false);
		}
		textInputView.setSendController(sendController);
		textInputView.setMaxTextLength(MAX_PRIVATE_MESSAGE_TEXT_LENGTH);
		textInputView.setReady(false);
		textInputView.setOnKeyboardShownListener(this::scrollToBottom);
		viewModel.getAutoDeleteTimer().observe(this, timer ->
				sendController.setAutoDeleteTimer(timer));
	}
	private void scrollToBottom() {
		int items = adapter.getItemCount();
		if (items > 0) list.scrollToPosition(items - 1);
	}
	@Override
	protected void onActivityResult(int request, int result,
			@Nullable Intent data) {
		super.onActivityResult(request, result, data);
		if (request == REQUEST_INTRODUCTION && result == RESULT_OK) {
			new BriarSnackbarBuilder()
					.make(list, R.string.introduction_sent,
							Snackbar.LENGTH_SHORT)
					.show();
		}
	}
	@Override
	public void onStart() {
		super.onStart();
		eventBus.addListener(this);
		notificationManager.blockContactNotification(contactId);
		notificationManager.clearContactNotification(contactId);
		displayContactOnlineStatus();
		viewModel.getContactDisplayName().observe(this, contactNameObserver);
		list.startPeriodicUpdate();
	}
	@Override
	public void onStop() {
		super.onStop();
		eventBus.removeListener(this);
		notificationManager.unblockContactNotification(contactId);
		viewModel.getContactDisplayName().removeObserver(contactNameObserver);
		list.stopPeriodicUpdate();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (layoutManager != null) {
			layoutManagerState = layoutManager.onSaveInstanceState();
			outState.putParcelable("layoutManager", layoutManagerState);
		}
		if (tracker != null) tracker.onSaveInstanceState(outState);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		layoutManagerState = savedInstanceState.getParcelable("layoutManager");
		if (tracker != null) tracker.onRestoreInstanceState(savedInstanceState);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.conversation_actions, menu);
		observeOnce(viewModel.showIntroductionAction(), this, enable -> {
			if (enable != null && enable) {
				menu.findItem(R.id.action_introduction).setEnabled(true);
				viewModel.showIntroductionOnboarding().observeEvent(this,
						this::showIntroductionOnboarding);
			}
		});
		observeOnce(viewModel.getContactItem(), this, contact -> {
			menu.findItem(R.id.action_set_alias).setEnabled(true);
			menu.findItem(R.id.action_connect_via_bluetooth).setEnabled(true);
		});
		if (featureFlags.shouldEnableDisappearingMessages()) {
			MenuItem item = menu.findItem(R.id.action_conversation_settings);
			item.setVisible(true);
			viewModel.getPrivateMessageFormat().observe(this, format ->
					item.setEnabled(format == TEXT_IMAGES_AUTO_DELETE));
		}
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			onBackPressed();
			return true;
		} else if (itemId == R.id.action_introduction) {
			Intent intent = new Intent(this, IntroductionActivity.class);
			intent.putExtra(CONTACT_ID, contactId.getInt());
			startActivityForResult(intent, REQUEST_INTRODUCTION);
			return true;
		} else if (itemId == R.id.action_set_alias) {
			AliasDialogFragment.newInstance().show(
					getSupportFragmentManager(), AliasDialogFragment.TAG);
			return true;
		} else if (itemId == R.id.action_conversation_settings) {
			onAutoDeleteTimerNoticeClicked();
			return true;
		} else if (itemId == R.id.action_connect_via_bluetooth) {
			Intent intent = new Intent(this, ConnectViaBluetoothActivity.class);
			intent.putExtra(CONTACT_ID, contactId.getInt());
			startActivity(intent);
			return true;
		} else if (itemId == R.id.action_transfer_data) {
			Intent intent = new Intent(this, RemovableDriveActivity.class);
			intent.putExtra(CONTACT_ID, contactId.getInt());
			startActivity(intent);
			return true;
		} else if (itemId == R.id.action_delete_all_messages) {
			askToDeleteAllMessages();
			return true;
		} else if (itemId == R.id.action_social_remove_person) {
			askToRemoveContact();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.conversation_message_actions, menu);
		return true;
	}
	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		if (item.getItemId() == R.id.action_delete) {
			deleteSelectedMessages();
			return true;
		}
		return false;
	}
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		tracker.clearSelection();
		actionMode = null;
	}
	@Override
	public void onLinkClick(String url) {
		LinkDialogFragment f = LinkDialogFragment.newInstance(url);
		f.show(getSupportFragmentManager(), f.getUniqueTag());
	}
	private void addSelectionTracker() {
		RecyclerView recyclerView = list.getRecyclerView();
		if (recyclerView.getAdapter() != adapter)
			throw new IllegalStateException();
		tracker = new SelectionTracker.Builder<>(
				"conversationSelection",
				recyclerView,
				new ConversationItemKeyProvider(adapter),
				new ConversationItemDetailsLookup(recyclerView),
				StorageStrategy.createStringStorage()
		).withSelectionPredicate(
				SelectionPredicates.createSelectAnything()
		).build();
		SelectionObserver<String> observer = new SelectionObserver<String>() {
			@Override
			public void onItemStateChanged(String key, boolean selected) {
				if (selected && actionMode == null) {
					actionMode = startActionMode(ConversationActivity.this);
					updateActionModeTitle();
				} else if (actionMode != null) {
					if (selected || tracker.hasSelection()) {
						updateActionModeTitle();
					} else {
						actionMode.finish();
					}
				}
			}
		};
		tracker.addObserver(observer);
		adapter.setSelectionTracker(tracker);
	}
	private void updateActionModeTitle() {
		if (actionMode == null) throw new IllegalStateException();
		String title = String.valueOf(tracker.getSelection().size());
		actionMode.setTitle(title);
	}
	private Collection<MessageId> getSelection() {
		Selection<String> selection = tracker.getSelection();
		List<MessageId> messages = new ArrayList<>(selection.size());
		for (String str : selection) {
			try {
				MessageId id = new MessageId(fromHexString(str));
				messages.add(id);
			} catch (FormatException e) {
				LOG.warning("Invalid message id");
			}
		}
		return messages;
	}
	@UiThread
	private void displayContactOnlineStatus() {
		if (connectionRegistry.isConnected(contactId)) {
			toolbarStatus.setImageResource(R.drawable.contact_online);
			toolbarStatus.setContentDescription(getString(R.string.online));
		} else {
			toolbarStatus.setImageResource(R.drawable.contact_offline);
			toolbarStatus.setContentDescription(getString(R.string.offline));
		}
	}
	private void loadMessages() {
		int revision = adapter.getRevision();
		runOnDbThread(() -> {
			try {
				long start = now();
				Collection<ConversationMessageHeader> headers =
						conversationManager.getMessageHeaders(contactId);
				logDuration(LOG, "Loading messages", start);
				List<ConversationMessageHeader> sorted =
						new ArrayList<>(headers);
				sort(sorted, (a, b) ->
						Long.compare(b.getTimestamp(), a.getTimestamp()));
				if (!sorted.isEmpty()) {
					ConversationMessageHeader latest = sorted.get(0);
					if (latest instanceof PrivateMessageHeader) {
						eagerlyLoadMessageSize((PrivateMessageHeader) latest);
					}
				}
				displayMessages(revision, sorted);
			} catch (NoSuchContactException e) {
				finishOnUiThread();
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}
	@DatabaseExecutor
	private void eagerlyLoadMessageSize(PrivateMessageHeader h) {
		try {
			MessageId id = h.getId();
			if (h.hasText()) {
				String text = textCache.get(id);
				if (text == null) {
					LOG.info("Eagerly loading text for latest message");
					text = messagingManager.getMessageText(id);
					textCache.put(id, requireNonNull(text));
				}
			}
			List<AttachmentHeader> headers = h.getAttachmentHeaders();
			if (headers.size() == 1) {
				LOG.info("Eagerly loading image size for latest message");
				AttachmentHeader header = headers.get(0);
				attachmentRetriever
						.cacheAttachmentItemWithSize(h.getId(), header);
			}
		} catch (DbException e) {
			logException(LOG, WARNING, e);
		}
	}
	private void displayMessages(int revision,
			Collection<ConversationMessageHeader> headers) {
		runOnUiThreadUnlessDestroyed(() -> {
			if (revision == adapter.getRevision()) {
				adapter.incrementRevision();
				textInputView.setReady(true);
				if (featureFlags.shouldEnableImageAttachments()) {
					viewModel.showImageOnboarding().observeEvent(this,
							this::showImageOnboarding);
				}
				List<ConversationItem> items = createItems(headers);
				adapter.replaceAll(items);
				list.showData();
				if (layoutManagerState == null) {
					scrollToBottom();
				} else {
					layoutManager.onRestoreInstanceState(layoutManagerState);
				}
			} else {
				LOG.info("Concurrent update, reloading");
				loadMessages();
			}
		});
	}
	private List<ConversationItem> createItems(
			Collection<ConversationMessageHeader> headers) {
		List<ConversationItem> items = new ArrayList<>(headers.size());
		for (ConversationMessageHeader h : headers)
			items.add(h.accept(visitor));
		return items;
	}
	private void loadMessageText(MessageId m) {
		runOnDbThread(() -> {
			try {
				long start = now();
				String text = messagingManager.getMessageText(m);
				logDuration(LOG, "Loading text", start);
				displayMessageText(m, requireNonNull(text));
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}
	private void displayMessageText(MessageId m, String text) {
		runOnUiThreadUnlessDestroyed(() -> {
			textCache.put(m, text);
			Pair<Integer, ConversationMessageItem> pair =
					adapter.getMessageItem(m);
			if (pair != null) {
				boolean scroll = shouldScrollWhenUpdatingMessage();
				pair.getSecond().setText(text);
				adapter.notifyItemChanged(pair.getFirst());
				if (scroll) scrollToBottom();
			}
		});
	}
	private boolean shouldScrollWhenUpdatingMessage() {
		return getLifecycle().getCurrentState().isAtLeast(STARTED)
				&& adapter.isScrolledToBottom(layoutManager);
	}
	@UiThread
	private void updateMessageAttachment(MessageId m, AttachmentItem item) {
		Pair<Integer, ConversationMessageItem> pair = adapter.getMessageItem(m);
		if (pair != null && pair.getSecond().updateAttachments(item)) {
			boolean scroll = shouldScrollWhenUpdatingMessage();
			adapter.notifyItemChanged(pair.getFirst());
			if (scroll) scrollToBottom();
		}
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactRemovedEvent) {
			ContactRemovedEvent c = (ContactRemovedEvent) e;
			if (c.getContactId().equals(contactId)) {
				LOG.info("Contact removed");
				supportFinishAfterTransition();
			}
		} else if (e instanceof ConversationMessageReceivedEvent) {
			ConversationMessageReceivedEvent<?> p =
					(ConversationMessageReceivedEvent<?>) e;
			if (p.getContactId().equals(contactId)) {
				LOG.info("Message received, adding");
				onNewConversationMessage(p.getMessageHeader());
			}
		} else if (e instanceof MessagesSentEvent) {
			MessagesSentEvent m = (MessagesSentEvent) e;
			if (m.getContactId().equals(contactId)) {
				LOG.info("Messages sent");
				markMessages(m.getMessageIds(), true, false);
			}
		} else if (e instanceof MessagesAckedEvent) {
			MessagesAckedEvent m = (MessagesAckedEvent) e;
			if (m.getContactId().equals(contactId)) {
				LOG.info("Messages acked");
				markMessages(m.getMessageIds(), true, true);
			}
		} else if (e instanceof ConversationMessagesDeletedEvent) {
			ConversationMessagesDeletedEvent m =
					(ConversationMessagesDeletedEvent) e;
			if (m.getContactId().equals(contactId)) {
				LOG.info("Messages auto-deleted");
				onConversationMessagesDeleted(m.getMessageIds());
			}
		} else if (e instanceof ContactConnectedEvent) {
			ContactConnectedEvent c = (ContactConnectedEvent) e;
			if (c.getContactId().equals(contactId)) {
				LOG.info("Contact connected");
				displayContactOnlineStatus();
			}
		} else if (e instanceof ContactDisconnectedEvent) {
			ContactDisconnectedEvent c = (ContactDisconnectedEvent) e;
			if (c.getContactId().equals(contactId)) {
				LOG.info("Contact disconnected");
				displayContactOnlineStatus();
			}
		} else if (e instanceof ClientVersionUpdatedEvent) {
			ClientVersionUpdatedEvent c = (ClientVersionUpdatedEvent) e;
			if (c.getContactId().equals(contactId)) {
				ClientId clientId = c.getClientVersion().getClientId();
				if (clientId.equals(MessagingManager.CLIENT_ID)) {
					LOG.info("Contact's messaging client was updated");
					viewModel.recheckFeaturesAndOnboarding(contactId);
				}
			}
		}
	}
	@UiThread
	private void addConversationItem(ConversationItem item) {
		adapter.incrementRevision();
		adapter.add(item);
		if (getLifecycle().getCurrentState().isAtLeast(STARTED))
			scrollToBottom();
	}
	@UiThread
	private void onNewConversationMessage(ConversationMessageHeader h) {
		if (h instanceof ConversationRequest ||
				h instanceof ConversationResponse) {
			observeOnce(viewModel.getContactDisplayName(), this,
					name -> addConversationItem(h.accept(visitor)));
		} else {
			addConversationItem(h.accept(visitor));
		}
	}
	@UiThread
	private void onConversationMessagesDeleted(
			Collection<MessageId> messageIds) {
		adapter.incrementRevision();
		adapter.removeItems(messageIds);
	}
	@UiThread
	private void markMessages(Collection<MessageId> messageIds, boolean sent,
			boolean seen) {
		adapter.incrementRevision();
		Set<MessageId> messages = new HashSet<>(messageIds);
		SparseArray<ConversationItem> list = adapter.getOutgoingMessages();
		for (int i = 0; i < list.size(); i++) {
			ConversationItem item = list.valueAt(i);
			if (messages.contains(item.getId())) {
				item.setSent(sent);
				item.setSeen(seen);
				adapter.notifyItemChanged(list.keyAt(i));
			}
		}
	}
	@Override
	public void onAttachImageClicked() {
		launchActivityToOpenFile(this, docLauncher, contentLauncher, "image
	@Override
	public List<AttachmentItem> getAttachmentItems(PrivateMessageHeader h) {
		List<LiveData<AttachmentItem>> liveDataList =
				attachmentRetriever.getAttachmentItems(h);
		List<AttachmentItem> items = new ArrayList<>(liveDataList.size());
		for (LiveData<AttachmentItem> liveData : liveDataList) {
			liveData.removeObservers(this);
			liveData.observe(this, new AttachmentObserver(h.getId(), liveData));
			items.add(requireNonNull(liveData.getValue()));
		}
		return items;
	}
	private class AttachmentObserver implements Observer<AttachmentItem> {
		private final MessageId conversationMessageId;
		private final LiveData<AttachmentItem> liveData;
		private AttachmentObserver(MessageId conversationMessageId,
				LiveData<AttachmentItem> liveData) {
			this.conversationMessageId = conversationMessageId;
			this.liveData = liveData;
		}
		@Override
		public void onChanged(AttachmentItem attachmentItem) {
			updateMessageAttachment(conversationMessageId, attachmentItem);
			if (attachmentItem.getState().isFinal())
				liveData.removeObserver(this);
		}
	}
}