package org.nodex.android.introduction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.contact.ContactListAdapter;
import org.nodex.android.contact.ContactListItem;
import org.nodex.android.contact.OnContactClickListener;
import org.nodex.android.fragment.BaseFragment;
import org.nodex.android.view.NodexRecyclerView;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ContactChooserFragment extends BaseFragment
		implements OnContactClickListener<ContactListItem> {
	private static final String TAG = ContactChooserFragment.class.getName();
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private IntroductionViewModel viewModel;
	private final ContactListAdapter adapter = new ContactListAdapter(this);
	private NodexRecyclerView list;
	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
				.get(IntroductionViewModel.class);
	}
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		requireActivity().setTitle(R.string.introduction_activity_title);
		View contentView = inflater.inflate(R.layout.list, container, false);
		list = contentView.findViewById(R.id.list);
		list.setLayoutManager(new LinearLayoutManager(getActivity()));
		list.setAdapter(adapter);
		list.setEmptyText(R.string.no_contacts);
		viewModel.getContactListItems().observe(getViewLifecycleOwner(),
				result -> result.onError(this::handleException)
						.onSuccess(adapter::submitList)
		);
		return contentView;
	}
	@Override
	public void onStart() {
		super.onStart();
		list.startPeriodicUpdate();
	}
	@Override
	public void onStop() {
		super.onStop();
		list.stopPeriodicUpdate();
	}
	@Override
	public String getUniqueTag() {
		return TAG;
	}
	@Override
	public void onItemClick(View view, ContactListItem item) {
		viewModel.setSecondContactId(item.getContact().getId());
		viewModel.triggerContactSelected();
	}
}