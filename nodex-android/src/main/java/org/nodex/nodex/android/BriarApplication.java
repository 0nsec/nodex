package org.nodex.android;
import android.app.Activity;
import android.content.SharedPreferences;
import org.nodex.core.BrambleApplication;
import org.nodex.android.navdrawer.NavDrawerActivity;
public interface BriarApplication extends BrambleApplication {
	Class<? extends Activity> ENTRY_ACTIVITY = NavDrawerActivity.class;
	AndroidComponent getApplicationComponent();
	SharedPreferences getDefaultSharedPreferences();
	boolean isRunningInBackground();
	boolean isInstrumentationTest();
}