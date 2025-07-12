package org.nodex.android.privategroup.reveal;
import android.content.Context;
import android.os.Bundle;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.contact.OnContactClickListener;
import org.nodex.android.contactselection.BaseContactSelectorFragment;
import org.nodex.android.contactselection.ContactSelectorController;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.Collection;
import javax.inject.Inject;
import static org.nodex.android.activity.NodexActivity.GROUP_ID;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class RevealContactsFragment extends
		BaseContactSelectorFragment<RevealableContactItem, RevealableContactAdapter> {
	private final static String TAG = RevealContactsFragment.class.getName();
	@Inject
	RevealContactsController controller;
	public static RevealContactsFragment newInstance(GroupId groupId) {
		Bundle args = new Bundle();
		args.putByteArray(GROUP_ID, groupId.getBytes());
		RevealContactsFragment fragment = new RevealContactsFragment();
		fragment.setArguments(args);
		return fragment;
	}
	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	protected ContactSelectorController<RevealableContactItem> getController() {
		return controller;
	}
	@Override
	protected RevealableContactAdapter getAdapter(Context context,
			OnContactClickListener<RevealableContactItem> listener) {
		return new RevealableContactAdapter(context, listener);
	}
	@Override
	protected void onSelectionChanged() {
		Collection<ContactId> selected = adapter.getSelectedContactIds();
		Collection<ContactId> disabled = adapter.getDisabledContactIds();
		selected.removeAll(disabled);
		listener.contactsSelected(selected);
	}
	@Override
	public String getUniqueTag() {
		return TAG;
	}
}