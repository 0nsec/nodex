package org.nodex.android.conversation;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import org.nodex.core.api.sync.MessageId;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.BaseActivity;
import org.nodex.android.attachment.AttachmentItem;
import org.nodex.android.conversation.glide.GlideApp;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import static android.widget.ImageView.ScaleType.FIT_START;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.NONE;
import static org.nodex.android.attachment.AttachmentItem.State.AVAILABLE;
import static org.nodex.android.attachment.AttachmentItem.State.ERROR;
import static org.nodex.android.conversation.ImageActivity.ATTACHMENT_POSITION;
import static org.nodex.android.conversation.ImageActivity.ITEM_ID;
import static org.nodex.nullsafety.NullSafety.requireNonNull;
@MethodsNotNullByDefault
@ParametersAreNonnullByDefault
public class ImageFragment extends Fragment
		implements RequestListener<Drawable> {
	private final static String IS_FIRST = "isFirst";
	@DrawableRes
	private static final int ERROR_RES = R.drawable.ic_image_broken;
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private AttachmentItem attachment;
	private boolean isFirst;
	private MessageId conversationItemId;
	private ImageViewModel viewModel;
	private PhotoView photoView;
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(requireActivity(),
				viewModelFactory).get(ImageViewModel.class);
	}
	static ImageFragment newInstance(AttachmentItem a,
			MessageId conversationMessageId, boolean isFirst) {
		ImageFragment f = new ImageFragment();
		Bundle args = new Bundle();
		args.putParcelable(ATTACHMENT_POSITION, a);
		args.putBoolean(IS_FIRST, isFirst);
		args.putByteArray(ITEM_ID, conversationMessageId.getBytes());
		f.setArguments(args);
		return f;
	}
	@Override
	public void onAttach(Context ctx) {
		super.onAttach(ctx);
		injectFragment(
				((BaseActivity) requireActivity()).getActivityComponent());
	}
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = requireArguments();
		attachment = requireNonNull(args.getParcelable(ATTACHMENT_POSITION));
		isFirst = args.getBoolean(IS_FIRST);
		conversationItemId =
				new MessageId(requireNonNull(args.getByteArray(ITEM_ID)));
	}
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_image, container,
				false);
		photoView = v.findViewById(R.id.photoView);
		photoView.setScaleLevels(1, 2, 4);
		photoView.setOnClickListener(view -> viewModel.clickImage());
		if (attachment.getState() == AVAILABLE) {
			loadImage();
		} else if (attachment.getState() == ERROR) {
			photoView.setImageResource(ERROR_RES);
			startPostponedTransition();
		} else {
			photoView.setImageResource(R.drawable.ic_image_missing);
			startPostponedTransition();
			LifecycleOwner owner = getViewLifecycleOwner();
			viewModel.getOnAttachmentReceived(attachment.getMessageId())
					.observeEvent(owner, this::onAttachmentReceived);
		}
		return v;
	}
	private void loadImage() {
		GlideApp.with(this)
				.load(attachment.getHeader())
				.diskCacheStrategy(NONE)
				.error(ERROR_RES)
				.addListener(this)
				.into(photoView);
	}
	private void onAttachmentReceived(Boolean received) {
		if (received) loadImage();
	}
	@Override
	public boolean onLoadFailed(@Nullable GlideException e,
			Object model, Target<Drawable> target,
			boolean isFirstResource) {
		startPostponedTransition();
		return false;
	}
	@Override
	public boolean onResourceReady(Drawable resource, Object model,
			Target<Drawable> target, DataSource dataSource,
			boolean isFirstResource) {
		if (!(resource instanceof Animatable)) {
			photoView.setTransitionName(
					attachment.getTransitionName(conversationItemId));
		}
		if (viewModel.isOverlappingToolbar(photoView, resource)) {
			photoView.setScaleType(FIT_START);
		}
		startPostponedTransition();
		return false;
	}
	private void startPostponedTransition() {
		if (getActivity() != null && isFirst) {
			getActivity().supportStartPostponedEnterTransition();
		}
	}
}