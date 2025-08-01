package org.nodex.android.contactselection;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.sync.GroupId;
import org.nodex.R;
import org.nodex.android.contact.ContactItemViewHolder;
import org.nodex.android.contact.OnContactClickListener;
import org.nodex.android.controller.handler.UiResultExceptionHandler;
import org.nodex.android.fragment.BaseFragment;
import org.nodex.android.view.NodexRecyclerView;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;
import androidx.annotation.CallSuper;
import androidx.recyclerview.widget.LinearLayoutManager;
import static org.nodex.android.activity.NodexActivity.GROUP_ID;
import static org.nodex.android.contactselection.ContactSelectorActivity.CONTACTS;
import static org.nodex.android.contactselection.ContactSelectorActivity.getContactsFromIds;
import static org.nodex.android.contactselection.ContactSelectorActivity.getContactsFromIntegers;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class BaseContactSelectorFragment<I extends BaseSelectableContactItem, A extends BaseContactSelectorAdapter<I, ? extends ContactItemViewHolder<I>>>
		extends BaseFragment
		implements OnContactClickListener<I> {
	protected NodexRecyclerView list;
	protected A adapter;
	protected Collection<ContactId> selectedContacts = new ArrayList<>();
	protected ContactSelectorListener listener;
	private GroupId groupId;
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		listener = (ContactSelectorListener) context;
	}
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = requireArguments();
		byte[] b = args.getByteArray(GROUP_ID);
		if (b == null) throw new IllegalStateException("No GroupId");
		groupId = new GroupId(b);
	}
	@Override
	@CallSuper
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.list, container, false);
		list = contentView.findViewById(R.id.list);
		list.setLayoutManager(new LinearLayoutManager(getActivity()));
		list.setEmptyImage(R.drawable.il_empty_state_contact_list);
		list.setEmptyText(getString(R.string.no_contacts_selector));
		list.setEmptyAction(getString(R.string.no_contacts_selector_action));
		adapter = getAdapter(requireContext(), this);
		list.setAdapter(adapter);
		if (savedInstanceState != null) {
			ArrayList<Integer> intContacts =
					savedInstanceState.getIntegerArrayList(CONTACTS);
			if (intContacts != null) {
				selectedContacts = getContactsFromIntegers(intContacts);
			}
		}
		return contentView;
	}
	protected abstract A getAdapter(Context context,
			OnContactClickListener<I> listener);
	@Override
	public void onStart() {
		super.onStart();
		loadContacts(selectedContacts);
	}
	@Override
	public void onStop() {
		super.onStop();
		adapter.clear();
		list.showProgressBar();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (adapter != null) {
			selectedContacts = adapter.getSelectedContactIds();
			outState.putIntegerArrayList(CONTACTS,
					getContactsFromIds(selectedContacts));
		}
	}
	@Override
	public void onItemClick(View view, I item) {
		item.toggleSelected();
		adapter.notifyItemChanged(adapter.findItemPosition(item), item);
		onSelectionChanged();
	}
	private void loadContacts(Collection<ContactId> selection) {
		getController().loadContacts(groupId, selection,
				new UiResultExceptionHandler<Collection<I>, DbException>(
						this) {
					@Override
					public void onResultUi(Collection<I> contacts) {
						if (contacts.isEmpty()) list.showData();
						else adapter.addAll(contacts);
						onSelectionChanged();
					}
					@Override
					public void onExceptionUi(DbException exception) {
						handleException(exception);
					}
				});
	}
	protected abstract void onSelectionChanged();
	protected abstract ContactSelectorController<I> getController();
}