package org.nodex.android.contactselection;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.nodex.R;
import org.nodex.android.contact.OnContactClickListener;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class ContactSelectorFragment extends
		BaseContactSelectorFragment<SelectableContactItem, ContactSelectorAdapter>
		implements OnContactClickListener<SelectableContactItem> {
	public static final String TAG = ContactSelectorFragment.class.getName();
	private Menu menu;
	@Override
	protected ContactSelectorAdapter getAdapter(Context context,
			OnContactClickListener<SelectableContactItem> listener) {
		return new ContactSelectorAdapter(context, listener);
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.contact_selection_actions, menu);
		super.onCreateOptionsMenu(menu, inflater);
		this.menu = menu;
		onSelectionChanged();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_contacts_selected) {
			selectedContacts = adapter.getSelectedContactIds();
			listener.contactsSelected(selectedContacts);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onSelectionChanged() {
		if (menu == null) return;
		MenuItem item = menu.findItem(R.id.action_contacts_selected);
		if (item == null) return;
		selectedContacts = adapter.getSelectedContactIds();
		if (selectedContacts.size() > 0) {
			item.setVisible(true);
		} else {
			item.setVisible(false);
		}
	}
}