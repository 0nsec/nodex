package org.nodex.android.privategroup.conversation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.nodex.R;
import org.nodex.android.threaded.BaseThreadItemViewHolder;
import org.nodex.android.threaded.ThreadItemAdapter;
import org.nodex.android.threaded.ThreadPostViewHolder;
import org.nodex.nullsafety.NotNullByDefault;
import androidx.annotation.LayoutRes;
import androidx.annotation.UiThread;
@UiThread
@NotNullByDefault
class GroupMessageAdapter extends ThreadItemAdapter<GroupMessageItem> {
	private boolean isCreator = false;
	GroupMessageAdapter(ThreadItemListener<GroupMessageItem> listener) {
		super(listener);
	}
	@LayoutRes
	@Override
	public int getItemViewType(int position) {
		GroupMessageItem item = getItem(position);
		return item.getLayout();
	}
	@Override
	public BaseThreadItemViewHolder<GroupMessageItem> onCreateViewHolder(
			ViewGroup parent, int type) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(type, parent, false);
		if (type == R.layout.list_item_group_join_notice) {
			return new JoinMessageItemViewHolder(v, isCreator);
		}
		return new ThreadPostViewHolder<>(v);
	}
	void setIsCreator(boolean isCreator) {
		this.isCreator = isCreator;
		notifyDataSetChanged();
	}
}