package org.nodex.android.forum;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.google.android.material.snackbar.Snackbar;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.fragment.BaseFragment;
import org.nodex.android.sharing.ForumInvitationActivity;
import org.nodex.android.util.NodexSnackbarBuilder;
import org.nodex.android.view.NodexRecyclerView;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import static com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE;
import static java.util.Objects.requireNonNull;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ForumListFragment extends BaseFragment implements
		OnClickListener {
	public final static String TAG = ForumListFragment.class.getName();
	private ForumListViewModel viewModel;
	private NodexRecyclerView list;
	private Snackbar snackbar;
	private ForumListAdapter adapter;
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	public static ForumListFragment newInstance() {
		return new ForumListFragment();
	}
	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(ForumListViewModel.class);
		adapter = new ForumListAdapter(viewModel);
	}
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		requireActivity().setTitle(R.string.forums_button);
		View v = inflater.inflate(R.layout.fragment_forum_list, container,
				false);
		list = v.findViewById(R.id.forumList);
		list.setLayoutManager(new LinearLayoutManager(getActivity()));
		list.setAdapter(adapter);
		viewModel.getForumListItems().observe(getViewLifecycleOwner(), result ->
				result.onError(this::handleException).onSuccess(items -> {
					adapter.submitList(items);
					if (requireNonNull(items).size() == 0) list.showData();
				})
		);
		snackbar = new NodexSnackbarBuilder()
				.setAction(R.string.show, this)
				.make(list, "", LENGTH_INDEFINITE);
		viewModel.getNumInvitations().observe(getViewLifecycleOwner(), num -> {
			if (num == 0) {
				snackbar.dismiss();
			} else {
				snackbar.setText(getResources().getQuantityString(
						R.plurals.forums_shared, num, num));
				if (!snackbar.isShownOrQueued()) snackbar.show();
			}
		});
		return v;
	}
	@Override
	public String getUniqueTag() {
		return TAG;
	}
	@Override
	public void onStart() {
		super.onStart();
		viewModel.blockAllForumPostNotifications();
		viewModel.clearAllForumPostNotifications();
		viewModel.loadForums();
		viewModel.loadForumInvitations();
		list.startPeriodicUpdate();
	}
	@Override
	public void onStop() {
		super.onStop();
		list.stopPeriodicUpdate();
		viewModel.unblockAllForumPostNotifications();
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.forum_list_actions, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_create_forum) {
			Intent intent = new Intent(getContext(), CreateForumActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onClick(View view) {
		Intent i = new Intent(getContext(), ForumInvitationActivity.class);
		startActivity(i);
	}
}