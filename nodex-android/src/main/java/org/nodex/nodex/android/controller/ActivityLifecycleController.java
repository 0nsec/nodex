package org.nodex.android.controller;
import android.app.Activity;
public interface ActivityLifecycleController {
	void onActivityCreate(Activity activity);
	void onActivityStart();
	void onActivityStop();
	void onActivityDestroy();
}