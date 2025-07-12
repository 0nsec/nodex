package org.nodex.android.contactselection;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import org.nodex.R;
import org.nodex.android.contact.ContactItemViewHolder;
import org.nodex.android.contact.OnContactClickListener;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import androidx.annotation.UiThread;
import static org.nodex.android.util.UiUtils.GREY_OUT;
@UiThread
@NotNullByDefault
public abstract class BaseSelectableContactHolder<I extends BaseSelectableContactItem>
		extends ContactItemViewHolder<I> {
	private final CheckBox checkBox;
	protected final TextView info;
	public BaseSelectableContactHolder(View v) {
		super(v);
		checkBox = v.findViewById(R.id.checkBox);
		info = v.findViewById(R.id.infoView);
	}
	@Override
	protected void bind(I item, @Nullable
			OnContactClickListener<I> listener) {
		super.bind(item, listener);
		if (item.isSelected()) {
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
		}
		if (item.isDisabled()) {
			layout.setEnabled(false);
			grayOutItem(true);
		} else {
			layout.setEnabled(true);
			grayOutItem(false);
		}
	}
	protected void grayOutItem(boolean gray) {
		float alpha = gray ? GREY_OUT : 1f;
		avatar.setAlpha(alpha);
		name.setAlpha(alpha);
		checkBox.setAlpha(alpha);
		info.setAlpha(alpha);
	}
}