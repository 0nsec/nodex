package org.nodex.android.contact.add.remote;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.MenuItem;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import org.nodex.core.api.contact.PendingContactId;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.NodexActivity;
import org.nodex.android.util.NodexSnackbarBuilder;
import org.nodex.android.view.NodexRecyclerView;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import static com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE;
import static org.nodex.core.api.contact.PendingContactState.FAILED;
import static org.nodex.android.contact.add.remote.PendingContactItem.POLL_DURATION_MS;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class PendingContactListActivity extends NodexActivity
		implements PendingContactListener {
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private PendingContactListViewModel viewModel;
	private PendingContactListAdapter adapter;
	private NodexRecyclerView list;
	private Snackbar offlineSnackbar;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(PendingContactListViewModel.class);
	}
	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.list);
		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setDisplayHomeAsUpEnabled(true);
		}
		viewModel.onCreate();
		viewModel.getPendingContacts()
				.observe(this, this::onPendingContactsChanged);
		viewModel.getHasInternetConnection()
				.observe(this, this::onInternetConnectionChanged);
		adapter = new PendingContactListAdapter(this, this,
				PendingContactItem.class);
		list = findViewById(R.id.list);
		list.setEmptyText(R.string.no_pending_contacts);
		list.setLayoutManager(new LinearLayoutManager(this));
		list.setAdapter(adapter);
		list.showProgressBar();
		offlineSnackbar = new NodexSnackbarBuilder()
				.setBackgroundColor(R.color.briar_red_500)
				.make(list, R.string.offline_state, LENGTH_INDEFINITE);
	}
	@Override
	public void onStart() {
		super.onStart();
		list.startPeriodicUpdate(POLL_DURATION_MS);
	}
	@Override
	protected void onStop() {
		super.onStop();
		list.stopPeriodicUpdate();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onPendingContactItemRemoved(PendingContactItem item) {
		if (item.getState() == FAILED) {
			removePendingContact(item.getPendingContact().getId());
			return;
		}
		OnClickListener removeListener = (dialog, which) ->
				removePendingContact(item.getPendingContact().getId());
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
				PendingContactListActivity.this, R.style.NodexDialogTheme);
		builder.setTitle(
				getString(R.string.dialog_title_remove_pending_contact));
		builder.setMessage(
				getString(R.string.dialog_message_remove_pending_contact));
		builder.setNegativeButton(R.string.groups_remove, removeListener);
		builder.setPositiveButton(R.string.cancel, null);
		builder.setOnDismissListener(dialog -> adapter.notifyDataSetChanged());
		builder.show();
	}
	private void removePendingContact(PendingContactId id) {
		viewModel.removePendingContact(id);
	}
	private void onPendingContactsChanged(
			Collection<PendingContactItem> items) {
		if (items.isEmpty()) {
			if (adapter.isEmpty()) {
				list.showData();
			} else {
				supportFinishAfterTransition();
			}
		} else {
			adapter.setItems(items);
		}
	}
	private void onInternetConnectionChanged(boolean online) {
		if (online) offlineSnackbar.dismiss();
		else offlineSnackbar.show();
	}
}