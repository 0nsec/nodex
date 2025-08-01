package org.nodex.android.conversation;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import org.nodex.R;
import org.nodex.android.attachment.AttachmentItem;
import org.nodex.nullsafety.NotNullByDefault;
import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool;
import static androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT;
import static androidx.core.content.ContextCompat.getColor;
import static androidx.core.widget.ImageViewCompat.setImageTintList;
@UiThread
@NotNullByDefault
class ConversationMessageViewHolder extends ConversationItemViewHolder {
	private final ImageAdapter adapter;
	private final ViewGroup statusLayout;
	private final int timeColor, timeColorBubble;
	private final ConstraintSet textConstraints = new ConstraintSet();
	private final ConstraintSet imageConstraints = new ConstraintSet();
	private final ConstraintSet imageTextConstraints = new ConstraintSet();
	ConversationMessageViewHolder(View v, ConversationListener listener,
			boolean isIncoming, RecycledViewPool imageViewPool,
			ImageItemDecoration imageItemDecoration) {
		super(v, listener, isIncoming);
		statusLayout = v.findViewById(R.id.statusLayout);
		RecyclerView list = v.findViewById(R.id.imageList);
		list.setRecycledViewPool(imageViewPool);
		adapter = new ImageAdapter(v.getContext(), listener);
		list.setAdapter(adapter);
		list.addItemDecoration(imageItemDecoration);
		timeColor = time.getCurrentTextColor();
		timeColorBubble =
				getColor(v.getContext(), R.color.msg_status_bubble_foreground);
		textConstraints.clone(v.getContext(),
				R.layout.list_item_conversation_msg_in_content);
		imageConstraints.clone(v.getContext(),
				R.layout.list_item_conversation_msg_image);
		imageTextConstraints.clone(v.getContext(),
				R.layout.list_item_conversation_msg_image_text);
		textConstraints
				.setHorizontalBias(R.id.statusLayout, isIncoming() ? 1 : 0);
		imageConstraints
				.setHorizontalBias(R.id.statusLayout, isIncoming() ? 1 : 0);
		imageTextConstraints
				.setHorizontalBias(R.id.statusLayout, isIncoming() ? 1 : 0);
	}
	@Override
	void bind(ConversationItem conversationItem, boolean selected) {
		super.bind(conversationItem, selected);
		ConversationMessageItem item =
				(ConversationMessageItem) conversationItem;
		if (item.getAttachments().isEmpty()) {
			bindTextItem();
		} else {
			bindImageItem(item);
		}
	}
	private void bindTextItem() {
		resetStatusLayoutForText();
		textConstraints.applyTo(layout);
		adapter.clear();
	}
	private void bindImageItem(ConversationMessageItem item) {
		ConstraintSet constraintSet;
		if (item.getText() == null) {
			statusLayout.setBackgroundResource(R.drawable.msg_status_bubble);
			time.setTextColor(timeColorBubble);
			setImageTintList(bomb, ColorStateList.valueOf(timeColorBubble));
			constraintSet = imageConstraints;
		} else {
			resetStatusLayoutForText();
			constraintSet = imageTextConstraints;
		}
		if (item.getAttachments().size() == 1) {
			AttachmentItem attachment = item.getAttachments().get(0);
			int width = attachment.getThumbnailWidth();
			int height = attachment.getThumbnailHeight();
			constraintSet.constrainWidth(R.id.imageList, width);
			constraintSet.constrainHeight(R.id.imageList, height);
		} else {
			constraintSet.constrainWidth(R.id.imageList, WRAP_CONTENT);
			constraintSet.constrainHeight(R.id.imageList, WRAP_CONTENT);
		}
		constraintSet.applyTo(layout);
		adapter.setConversationItem(item);
	}
	private void resetStatusLayoutForText() {
		statusLayout.setBackgroundResource(0);
		statusLayout.setPadding(0, 0, 0, 0);
		time.setTextColor(timeColor);
		setImageTintList(bomb, ColorStateList.valueOf(timeColor));
	}
}