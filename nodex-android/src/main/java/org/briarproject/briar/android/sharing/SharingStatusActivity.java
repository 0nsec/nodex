package org.nodex.android.sharing;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import org.nodex.core.api.connection.ConnectionRegistry;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.event.GroupRemovedEvent;
import org.nodex.R;
import org.nodex.android.activity.BriarActivity;
import org.nodex.android.contact.ContactItem;
import org.nodex.android.view.BriarRecyclerView;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorManager;
import org.nodex.api.sharing.event.ContactLeftShareableEvent;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.annotation.CallSuper;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.util.LogUtils.logException;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
abstract class SharingStatusActivity extends BriarActivity
		implements EventListener {
	@Inject
	volatile AuthorManager authorManager;
	@Inject
	volatile ConnectionRegistry connectionRegistry;
	@Inject
	EventBus eventBus;
	private static final Logger LOG =
			Logger.getLogger(SharingStatusActivity.class.getName());
	private GroupId groupId;
	private BriarRecyclerView list;
	private SharingStatusAdapter adapter;
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sharing_status);
		Intent i = getIntent();
		byte[] b = i.getByteArrayExtra(GROUP_ID);
		if (b == null) throw new IllegalStateException("No GroupId");
		groupId = new GroupId(b);
		list = findViewById(R.id.list);
		adapter = new SharingStatusAdapter(this);
		list.setLayoutManager(new LinearLayoutManager(this));
		list.setAdapter(adapter);
		list.setEmptyText(getString(R.string.nobody));
		TextView info = findViewById(R.id.info);
		info.setText(getInfoText());
	}
	@Override
	public void onStart() {
		super.onStart();
		eventBus.addListener(this);
		loadSharedWith();
	}
	@Override
	public void onStop() {
		super.onStop();
		adapter.clear();
		eventBus.removeListener(this);
		list.showProgressBar();
	}
	@Override
	@CallSuper
	public void eventOccurred(Event e) {
		if (e instanceof ContactLeftShareableEvent) {
			ContactLeftShareableEvent c = (ContactLeftShareableEvent) e;
			if (c.getGroupId().equals(getGroupId())) {
				loadSharedWith();
			}
		} else if (e instanceof GroupRemovedEvent) {
			GroupRemovedEvent g = (GroupRemovedEvent) e;
			if (g.getGroup().getId().equals(getGroupId())) {
				supportFinishAfterTransition();
			}
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@StringRes
	abstract int getInfoText();
	@DatabaseExecutor
	abstract protected Collection<Contact> getSharedWith() throws DbException;
	protected GroupId getGroupId() {
		return groupId;
	}
	protected void loadSharedWith() {
		runOnDbThread(() -> {
			try {
				List<ContactItem> contactItems = new ArrayList<>();
				for (Contact c : getSharedWith()) {
					AuthorInfo authorInfo = authorManager.getAuthorInfo(c);
					boolean online = connectionRegistry.isConnected(c.getId());
					ContactItem item = new ContactItem(c, authorInfo, online);
					contactItems.add(item);
				}
				displaySharedWith(contactItems);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}
	private void displaySharedWith(List<ContactItem> contacts) {
		runOnUiThreadUnlessDestroyed(() -> {
			adapter.clear();
			if (contacts.isEmpty()) list.showData();
			else adapter.addAll(contacts);
		});
	}
}