package org.nodex.android.conversation;
import org.nodex.android.view.BriarRecyclerViewScrollListener;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
class ConversationScrollListener extends
		BriarRecyclerViewScrollListener<ConversationAdapter, ConversationItem> {
	private final ConversationViewModel viewModel;
	protected ConversationScrollListener(ConversationAdapter adapter,
			ConversationViewModel viewModel) {
		super(adapter);
		this.viewModel = viewModel;
	}
	@Override
	protected void onItemVisible(ConversationItem item) {
		if (!item.isRead()) {
			viewModel.markMessageRead(item.getGroupId(), item.getId());
			item.markRead();
		}
	}
}