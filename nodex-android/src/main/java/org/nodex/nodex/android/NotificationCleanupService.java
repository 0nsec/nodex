package org.nodex.android;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import org.nodex.api.android.AndroidNotificationManager;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import static org.nodex.android.navdrawer.NavDrawerActivity.BLOG_URI;
import static org.nodex.android.navdrawer.NavDrawerActivity.CONTACT_ADDED_URI;
import static org.nodex.android.navdrawer.NavDrawerActivity.CONTACT_URI;
import static org.nodex.android.navdrawer.NavDrawerActivity.FORUM_URI;
import static org.nodex.android.navdrawer.NavDrawerActivity.GROUP_URI;
public class NotificationCleanupService extends IntentService {
	private static final String TAG =
			NotificationCleanupService.class.getName();
	@Inject
	AndroidNotificationManager notificationManager;
	public NotificationCleanupService() {
		super(TAG);
	}
	@Override
	public void onCreate() {
		super.onCreate();
		AndroidComponent applicationComponent =
				((NodexApplication) getApplication()).getApplicationComponent();
		applicationComponent.inject(this);
	}
	@Override
	protected void onHandleIntent(@Nullable Intent i) {
		if (i == null || i.getData() == null) return;
		Uri uri = i.getData();
		if (uri.equals(CONTACT_URI)) {
			notificationManager.clearAllContactNotifications();
		} else if (uri.equals(GROUP_URI)) {
			notificationManager.clearAllGroupMessageNotifications();
		} else if (uri.equals(FORUM_URI)) {
			notificationManager.clearAllForumPostNotifications();
		} else if (uri.equals(BLOG_URI)) {
			notificationManager.clearAllBlogPostNotifications();
		} else if (uri.equals(CONTACT_ADDED_URI)) {
			notificationManager.clearAllContactAddedNotifications();
		}
	}
}