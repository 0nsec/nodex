package org.nodex.android.introduction;
import android.app.Application;
import android.widget.Toast;
import org.nodex.core.api.connection.ConnectionRegistry;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.R;
import org.nodex.android.contact.ContactItem;
import org.nodex.android.contact.ContactsViewModel;
import org.nodex.android.viewmodel.LiveEvent;
import org.nodex.android.viewmodel.MutableLiveEvent;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorManager;
import org.nodex.api.introduction.IntroductionManager;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static android.widget.Toast.LENGTH_SHORT;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logException;
@NotNullByDefault
class IntroductionViewModel extends ContactsViewModel {
	private static final Logger LOG =
			getLogger(IntroductionViewModel.class.getName());
	private final ContactManager contactManager;
	private final AuthorManager authorManager;
	private final IntroductionManager introductionManager;
	@Inject
	IntroductionViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, TransactionManager db,
			AndroidExecutor androidExecutor, ContactManager contactManager,
			AuthorManager authorManager,
			ConversationManager conversationManager,
			ConnectionRegistry connectionRegistry, EventBus eventBus,
			IntroductionManager introductionManager) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor,
				contactManager, authorManager, conversationManager,
				connectionRegistry, eventBus);
		this.contactManager = contactManager;
		this.authorManager = authorManager;
		this.introductionManager = introductionManager;
	}
	@Nullable
	private ContactId firstContactId;
	@Nullable
	private ContactId secondContactId;
	private final MutableLiveEvent<Boolean> secondContactSelected =
			new MutableLiveEvent<>();
	private final MutableLiveData<IntroductionInfo> introductionInfo =
			new MutableLiveData<>();
	void setFirstContactId(ContactId contactId) {
		this.firstContactId = contactId;
		loadContacts();
	}
	@Nullable
	ContactId getSecondContactId() {
		return secondContactId;
	}
	void setSecondContactId(ContactId contactId) {
		secondContactId = contactId;
		introductionInfo.setValue(null);
		loadIntroductionInfo();
	}
	void triggerContactSelected() {
		secondContactSelected.setEvent(true);
	}
	LiveEvent<Boolean> getSecondContactSelected() {
		return secondContactSelected;
	}
	LiveData<IntroductionInfo> getIntroductionInfo() {
		return introductionInfo;
	}
	@Override
	protected boolean displayContact(ContactId contactId) {
		return !requireNonNull(firstContactId).equals(contactId);
	}
	private void loadIntroductionInfo() {
		final ContactId firstContactId = requireNonNull(this.firstContactId);
		final ContactId secondContactId = requireNonNull(this.secondContactId);
		runOnDbThread(() -> {
			try {
				Contact firstContact =
						contactManager.getContact(firstContactId);
				Contact secondContact =
						contactManager.getContact(secondContactId);
				AuthorInfo a1 = authorManager.getAuthorInfo(firstContact);
				AuthorInfo a2 = authorManager.getAuthorInfo(secondContact);
				boolean possible = introductionManager
						.canIntroduce(firstContact, secondContact);
				ContactItem c1 = new ContactItem(firstContact, a1);
				ContactItem c2 = new ContactItem(secondContact, a2);
				introductionInfo.postValue(
						new IntroductionInfo(c1, c2, possible));
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	void makeIntroduction(@Nullable String text) {
		final IntroductionInfo info =
				requireNonNull(introductionInfo.getValue());
		runOnDbThread(() -> {
			try {
				introductionManager.makeIntroduction(
						info.getContact1().getContact(),
						info.getContact2().getContact(), text);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				androidExecutor.runOnUiThread(() -> Toast.makeText(
						getApplication(), R.string.introduction_error,
						LENGTH_SHORT).show());
			}
		});
	}
}