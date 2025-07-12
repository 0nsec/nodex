package org.nodex.android.panic;
import android.os.Bundle;
import android.view.MenuItem;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.NodexActivity;
import androidx.appcompat.app.ActionBar;
public class PanicPreferencesActivity extends NodexActivity {
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		setContentView(R.layout.activity_panic_preferences);
	}
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return false;
	}
}