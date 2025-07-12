package org.nodex.android.contactselection;
import android.os.Bundle;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.sync.GroupId;
import org.nodex.R;
import org.nodex.android.activity.NodexActivity;
import org.nodex.android.fragment.BaseFragment.BaseFragmentListener;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.UiThread;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class ContactSelectorActivity
		extends NodexActivity
		implements BaseFragmentListener, ContactSelectorListener {
	protected final static String CONTACTS = "contacts";
	protected GroupId groupId;
	protected Collection<ContactId> contacts;
	@Override
	public void onCreate(@Nullable Bundle bundle) {
		super.onCreate(bundle);
		setContentView(getLayout());
		if (bundle != null) {
			byte[] groupBytes = bundle.getByteArray(GROUP_ID);
			if (groupBytes != null) groupId = new GroupId(groupBytes);
			ArrayList<Integer> intContacts =
					bundle.getIntegerArrayList(CONTACTS);
			if (intContacts != null) {
				contacts = getContactsFromIntegers(intContacts);
			}
		}
	}
	@LayoutRes
	protected int getLayout() {
		return R.layout.activity_fragment_container;
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (groupId != null) {
			outState.putByteArray(GROUP_ID, groupId.getBytes());
		}
		if (contacts != null) {
			outState.putIntegerArrayList(CONTACTS,
					getContactsFromIds(contacts));
		}
	}
	@CallSuper
	@UiThread
	@Override
	public void contactsSelected(Collection<ContactId> contacts) {
		this.contacts = contacts;
	}
	static ArrayList<Integer> getContactsFromIds(
			Collection<ContactId> contacts) {
		ArrayList<Integer> intContacts = new ArrayList<>(contacts.size());
		for (ContactId contactId : contacts) {
			intContacts.add(contactId.getInt());
		}
		return intContacts;
	}
	static Collection<ContactId> getContactsFromIntegers(
			ArrayList<Integer> intContacts) {
		List<ContactId> contacts = new ArrayList<>(intContacts.size());
		for (Integer c : intContacts) {
			contacts.add(new ContactId(c));
		}
		return contacts;
	}
}