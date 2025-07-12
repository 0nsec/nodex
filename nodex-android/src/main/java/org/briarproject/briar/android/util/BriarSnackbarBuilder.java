package org.nodex.android.util;
import android.view.View;
import android.view.View.OnClickListener;
import com.google.android.material.snackbar.Snackbar;
import org.nodex.R;
import org.nodex.nullsafety.NotNullByDefault;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import static androidx.core.content.ContextCompat.getColor;
@NotNullByDefault
public class BriarSnackbarBuilder {
	@ColorRes
	@Nullable
	private Integer backgroundResId = null;
	@StringRes
	private int actionResId;
	@Nullable
	private OnClickListener onClickListener;
	public Snackbar make(View view, CharSequence text, int duration) {
		Snackbar s = Snackbar.make(view, text, duration);
		if (backgroundResId != null) {
			s.setBackgroundTint(getColor(view.getContext(), backgroundResId));
			s.setTextColor(
					getColor(view.getContext(), R.color.md_theme_onSecondary));
		}
		if (onClickListener != null) {
			s.setActionTextColor(getColor(view.getContext(),
					R.color.briar_button_text_positive));
			s.setAction(actionResId, onClickListener);
		}
		return s;
	}
	public Snackbar make(View view, @StringRes int resId, int duration) {
		return make(view, view.getResources().getText(resId), duration);
	}
	public BriarSnackbarBuilder setBackgroundColor(
			@ColorRes int backgroundResId) {
		this.backgroundResId = backgroundResId;
		return this;
	}
	public BriarSnackbarBuilder setAction(@StringRes int actionResId,
			OnClickListener onClickListener) {
		this.actionResId = actionResId;
		this.onClickListener = onClickListener;
		return this;
	}
}