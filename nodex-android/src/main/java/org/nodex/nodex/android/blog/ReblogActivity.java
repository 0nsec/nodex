package org.nodex.android.blog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.MessageId;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.NodexActivity;
import org.nodex.android.fragment.BaseFragment.BaseFragmentListener;
import static org.nodex.android.blog.BlogPostFragment.POST_ID;
public class ReblogActivity extends NodexActivity implements
		BaseFragmentListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		byte[] groupId = intent.getByteArrayExtra(GROUP_ID);
		if (groupId == null)
			throw new IllegalArgumentException("No group ID in intent");
		byte[] postId = intent.getByteArrayExtra(POST_ID);
		if (postId == null)
			throw new IllegalArgumentException("No post message ID in intent");
		setContentView(R.layout.activity_fragment_container);
		if (savedInstanceState == null) {
			ReblogFragment f = ReblogFragment
					.newInstance(new GroupId(groupId), new MessageId(postId));
			showInitialFragment(f);
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
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
}