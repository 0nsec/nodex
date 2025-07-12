package org.nodex.android.privategroup.reveal;
import android.view.View;
import android.widget.ImageView;
import org.nodex.R;
import org.nodex.android.contact.OnContactClickListener;
import org.nodex.android.contactselection.BaseSelectableContactHolder;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import androidx.annotation.UiThread;
import static org.nodex.android.privategroup.VisibilityHelper.getVisibilityIcon;
import static org.nodex.android.privategroup.VisibilityHelper.getVisibilityString;
import static org.nodex.android.util.UiUtils.GREY_OUT;
import static org.nodex.android.util.UiUtils.getContactDisplayName;
@UiThread
@NotNullByDefault
class RevealableContactViewHolder
		extends BaseSelectableContactHolder<RevealableContactItem> {
	private final ImageView icon;
	RevealableContactViewHolder(View v) {
		super(v);
		icon = v.findViewById(R.id.visibilityView);
	}
	@Override
	protected void bind(RevealableContactItem item, @Nullable
			OnContactClickListener<RevealableContactItem> listener) {
		super.bind(item, listener);
		icon.setImageResource(getVisibilityIcon(item.getVisibility()));
		info.setText(
				getVisibilityString(info.getContext(), item.getVisibility(),
						getContactDisplayName(item.getContact())));
	}
	@Override
	protected void grayOutItem(boolean gray) {
		super.grayOutItem(gray);
		float alpha = gray ? GREY_OUT : 1f;
		icon.setAlpha(alpha);
	}
}