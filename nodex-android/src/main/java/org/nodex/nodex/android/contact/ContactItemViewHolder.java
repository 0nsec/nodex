package org.nodex.android.contact;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.nodex.R;
import org.nodex.android.view.TrustIndicatorView;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;
import static org.nodex.android.util.UiUtils.getContactDisplayName;
import static org.nodex.android.view.AuthorView.setAvatar;
@UiThread
@NotNullByDefault
public class ContactItemViewHolder<I extends ContactItem>
		extends RecyclerView.ViewHolder {
	protected final ViewGroup layout;
	protected final ImageView avatar;
	protected final TextView name;
	@Nullable
	protected final ImageView bulb;
	@Nullable
	protected final TrustIndicatorView trustIndicator;
	@Nullable
	protected final TextView trustIndicatorDescription;
	public ContactItemViewHolder(View v) {
		super(v);
		layout = (ViewGroup) v;
		avatar = v.findViewById(R.id.avatarView);
		name = v.findViewById(R.id.nameView);
		bulb = v.findViewById(R.id.bulbView);
		trustIndicator = v.findViewById(R.id.trustIndicator);
		trustIndicatorDescription =
				v.findViewById(R.id.trustIndicatorDescription);
	}
	protected void bind(I item, @Nullable OnContactClickListener<I> listener) {
		setAvatar(avatar, item);
		name.setText(getContactDisplayName(item.getContact()));
		if (bulb != null) {
			if (item.isConnected()) {
				bulb.setImageResource(R.drawable.contact_connected);
			} else {
				bulb.setImageResource(R.drawable.contact_disconnected);
			}
		}
		if (trustIndicator != null && trustIndicatorDescription != null) {
			final AuthorInfo.Status status = item.getAuthorInfo().getStatus();
			trustIndicator.setTrustLevel(status);
			switch (status) {
				case UNVERIFIED:
					trustIndicatorDescription.setText(
							R.string.peer_trust_level_unverified);
					break;
				case VERIFIED:
					trustIndicatorDescription.setText(
							R.string.peer_trust_level_verified);
					break;
				case OURSELVES:
					trustIndicatorDescription.setText(
							R.string.peer_trust_level_ourselves);
					break;
				default:
					trustIndicatorDescription.setText(
							R.string.peer_trust_level_stranger);
			}
		}
		layout.setOnClickListener(v -> {
			if (listener != null) listener.onItemClick(avatar, item);
		});
	}
}