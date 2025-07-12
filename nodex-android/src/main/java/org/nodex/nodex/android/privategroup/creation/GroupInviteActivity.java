package org.nodex.android.privategroup.creation;
import android.content.Intent;
import android.os.Bundle;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.contactselection.ContactSelectorActivity;
import org.nodex.android.controller.handler.UiResultExceptionHandler;
import org.nodex.android.sharing.BaseMessageFragment.MessageFragmentListener;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.inject.Inject;
import static org.nodex.api.privategroup.PrivateGroupConstants.MAX_GROUP_INVITATION_TEXT_LENGTH;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class GroupInviteActivity extends ContactSelectorActivity
		implements MessageFragmentListener {
	@Inject
	CreateGroupController controller;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	public void onCreate(@Nullable Bundle bundle) {
		super.onCreate(bundle);
		Intent i = getIntent();
		byte[] g = i.getByteArrayExtra(GROUP_ID);
		if (g == null) throw new IllegalStateException("No GroupId in intent");
		groupId = new GroupId(g);
		if (bundle == null) {
			showInitialFragment(GroupInviteFragment.newInstance(groupId));
		}
	}
	@Override
	public void contactsSelected(Collection<ContactId> contacts) {
		super.contactsSelected(contacts);
		showNextFragment(new CreateGroupMessageFragment());
	}
	@Override
	public void onButtonClick(@Nullable String text) {
		if (groupId == null)
			throw new IllegalStateException("GroupId was not initialized");
		controller.sendInvitation(groupId, contacts, text,
				new UiResultExceptionHandler<Void, DbException>(this) {
					@Override
					public void onResultUi(Void result) {
						setResult(RESULT_OK);
						supportFinishAfterTransition();
					}
					@Override
					public void onExceptionUi(DbException exception) {
						setResult(RESULT_CANCELED);
						handleException(exception);
					}
				});
	}
	@Override
	public int getMaximumTextLength() {
		return MAX_GROUP_INVITATION_TEXT_LENGTH;
	}
}