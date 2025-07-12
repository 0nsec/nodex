package org.nodex.android.blog;
import android.content.Intent;
import android.os.Bundle;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.MessageId;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.NodexActivity;
import org.nodex.android.fragment.BaseFragment;
import org.nodex.android.fragment.BaseFragment.BaseFragmentListener;
import org.nodex.android.sharing.BlogSharingStatusActivity;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import static java.util.Objects.requireNonNull;
import static org.nodex.android.blog.BlogPostFragment.POST_ID;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class BlogActivity extends NodexActivity
		implements BaseFragmentListener {
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private BlogViewModel viewModel;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(BlogViewModel.class);
	}
	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		Intent i = getIntent();
		GroupId groupId =
				new GroupId(requireNonNull(i.getByteArrayExtra(GROUP_ID)));
		@Nullable byte[] postId = i.getByteArrayExtra(POST_ID);
		viewModel.setGroupId(groupId, postId == null);
		setContentView(R.layout.activity_fragment_container_toolbar);
		Toolbar toolbar = setUpCustomToolbar(false);
		toolbar.setOnClickListener(v -> {
			Intent i1 = new Intent(BlogActivity.this,
					BlogSharingStatusActivity.class);
			i1.putExtra(GROUP_ID, groupId.getBytes());
			startActivity(i1);
		});
		viewModel.getBlog().observe(this, blog ->
				setTitle(blog.getBlog().getAuthor().getName())
		);
		viewModel.getSharingInfo().observe(this, info ->
				setToolbarSubTitle(info.total, info.online)
		);
		if (state == null) {
			if (postId == null) {
				showInitialFragment(BlogFragment.newInstance(groupId));
			} else {
				MessageId messageId = new MessageId(postId);
				BaseFragment f =
						BlogPostFragment.newInstance(groupId, messageId);
				showInitialFragment(f);
			}
		}
	}
	private void setToolbarSubTitle(int total, int online) {
		requireNonNull(getSupportActionBar())
				.setSubtitle(getString(R.string.shared_with, total, online));
	}
}