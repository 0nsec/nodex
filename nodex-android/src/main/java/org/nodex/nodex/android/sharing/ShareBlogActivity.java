package org.nodex.android.sharing;
import android.os.Bundle;
import android.widget.Toast;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DbException;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.controller.handler.UiExceptionHandler;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.inject.Inject;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nodex.api.sharing.SharingConstants.MAX_INVITATION_TEXT_LENGTH;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ShareBlogActivity extends ShareActivity {
	@Inject
	ShareBlogController controller;
	@Override
	BaseMessageFragment getMessageFragment() {
		return ShareBlogMessageFragment.newInstance();
	}
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	public void onCreate(@Nullable Bundle bundle) {
		super.onCreate(bundle);
		if (bundle == null) {
			showInitialFragment(ShareBlogFragment.newInstance(groupId));
		}
	}
	@Override
	public int getMaximumTextLength() {
		return MAX_INVITATION_TEXT_LENGTH;
	}
	@Override
	void share(Collection<ContactId> contacts, @Nullable String text) {
		controller.share(groupId, contacts, text,
				new UiExceptionHandler<DbException>(this) {
					@Override
					public void onExceptionUi(DbException exception) {
						Toast.makeText(ShareBlogActivity.this,
								R.string.blogs_sharing_error, LENGTH_SHORT)
								.show();
						handleException(exception);
					}
				});
	}
}