package org.nodex.android.privategroup.memberlist;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.event.GroupRemovedEvent;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.NodexActivity;
import org.nodex.android.controller.handler.UiResultExceptionHandler;
import org.nodex.android.view.NodexRecyclerView;
import org.nodex.api.privategroup.JoinMessageHeader;
import org.nodex.api.privategroup.event.GroupMessageAddedEvent;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.recyclerview.widget.LinearLayoutManager;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class GroupMemberListActivity extends NodexActivity
		implements EventListener {
	@Inject
	GroupMemberListController controller;
	@Inject
	EventBus eventBus;
	private MemberListAdapter adapter;
	private NodexRecyclerView list;
	private GroupId groupId;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_sharing_status);
		Intent i = getIntent();
		byte[] b = i.getByteArrayExtra(GROUP_ID);
		if (b == null) throw new IllegalStateException("No GroupId in intent.");
		groupId = new GroupId(b);
		list = findViewById(R.id.list);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(linearLayoutManager);
		adapter = new MemberListAdapter(this);
		list.setAdapter(adapter);
		TextView info = findViewById(R.id.info);
		info.setText(R.string.sharing_status_groups);
	}
	@Override
	public void onStart() {
		super.onStart();
		loadMembers();
		eventBus.addListener(this);
		list.startPeriodicUpdate();
	}
	@Override
	public void onStop() {
		super.onStop();
		eventBus.removeListener(this);
		list.stopPeriodicUpdate();
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof GroupMessageAddedEvent) {
			GroupMessageAddedEvent ge = (GroupMessageAddedEvent) e;
			if (ge.getGroupId().equals(groupId) &&
					ge.getHeader() instanceof JoinMessageHeader) {
				loadMembers();
			}
		} else if (e instanceof GroupRemovedEvent) {
			GroupRemovedEvent g = (GroupRemovedEvent) e;
			if (g.getGroup().getId().equals(groupId)) {
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
	private void loadMembers() {
		controller.loadMembers(groupId,
				new UiResultExceptionHandler<Collection<MemberListItem>, DbException>(
						this) {
					@Override
					public void onResultUi(Collection<MemberListItem> members) {
						adapter.addAll(members);
					}
					@Override
					public void onExceptionUi(DbException exception) {
						handleException(exception);
					}
				});
	}
}