package org.nodex.android.privategroup.reveal;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.settings.Settings;
import org.nodex.core.api.settings.SettingsManager;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.controller.DbControllerImpl;
import org.nodex.android.controller.handler.ExceptionHandler;
import org.nodex.android.controller.handler.ResultExceptionHandler;
import org.nodex.api.client.ProtocolStateException;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorManager;
import org.nodex.api.privategroup.GroupMember;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.invitation.GroupInvitationManager;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
import static org.nodex.api.privategroup.Visibility.INVISIBLE;
@Immutable
@NotNullByDefault
class RevealContactsControllerImpl extends DbControllerImpl
		implements RevealContactsController {
	private static final Logger LOG =
			Logger.getLogger(RevealContactsControllerImpl.class.getName());
	private static final String SHOW_ONBOARDING_REVEAL_CONTACTS =
			"showOnboardingRevealContacts";
	private final PrivateGroupManager groupManager;
	private final GroupInvitationManager groupInvitationManager;
	private final ContactManager contactManager;
	private final AuthorManager authorManager;
	private final SettingsManager settingsManager;
	@Inject
	RevealContactsControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, PrivateGroupManager groupManager,
			GroupInvitationManager groupInvitationManager,
			ContactManager contactManager, AuthorManager authorManager,
			SettingsManager settingsManager) {
		super(dbExecutor, lifecycleManager);
		this.groupManager = groupManager;
		this.groupInvitationManager = groupInvitationManager;
		this.contactManager = contactManager;
		this.authorManager = authorManager;
		this.settingsManager = settingsManager;
	}
	@Override
	public void loadContacts(GroupId g, Collection<ContactId> selection,
			ResultExceptionHandler<Collection<RevealableContactItem>, DbException> handler) {
		runOnDbThread(() -> {
			try {
				handler.onResult(getItems(g, selection));
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			}
		});
	}
	@DatabaseExecutor
	private Collection<RevealableContactItem> getItems(GroupId g,
			Collection<ContactId> selection) throws DbException {
		Collection<GroupMember> members = groupManager.getMembers(g);
		Collection<Contact> contacts = contactManager.getContacts();
		Collection<RevealableContactItem> items =
				new ArrayList<>(members.size());
		for (GroupMember m : members) {
			for (Contact c : contacts) {
				if (m.getAuthor().equals(c.getAuthor())) {
					AuthorInfo authorInfo = authorManager.getAuthorInfo(c);
					boolean disabled = m.getVisibility() != INVISIBLE;
					boolean selected =
							disabled || selection.contains(c.getId());
					items.add(new RevealableContactItem(c, authorInfo, selected,
							m.getVisibility()));
				}
			}
		}
		return items;
	}
	@Override
	public void isOnboardingNeeded(
			ResultExceptionHandler<Boolean, DbException> handler) {
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				handler.onResult(
						settings.getBoolean(SHOW_ONBOARDING_REVEAL_CONTACTS,
								true));
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			}
		});
	}
	@Override
	public void onboardingShown(ExceptionHandler<DbException> handler) {
		runOnDbThread(() -> {
			try {
				Settings settings = new Settings();
				settings.putBoolean(SHOW_ONBOARDING_REVEAL_CONTACTS, false);
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}
	@Override
	public void reveal(GroupId g, Collection<ContactId> contacts,
			ExceptionHandler<DbException> handler) {
		runOnDbThread(() -> {
			for (ContactId c : contacts) {
				try {
					groupInvitationManager.revealRelationship(c, g);
				} catch (ProtocolStateException e) {
					logException(LOG, INFO, e);
				} catch (DbException e) {
					logException(LOG, WARNING, e);
					handler.onException(e);
					break;
				}
			}
		});
	}
}