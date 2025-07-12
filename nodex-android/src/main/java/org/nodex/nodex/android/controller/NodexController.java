package org.nodex.android.controller;
import org.nodex.core.api.system.Wakeful;
import org.nodex.android.controller.handler.ResultHandler;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface NodexController extends ActivityLifecycleController {
	void startAndBindService();
	boolean accountSignedIn();
	void hasDozed(ResultHandler<Boolean> handler);
	void doNotAskAgainForDozeWhiteListing();
	@Wakeful
	void signOut(ResultHandler<Void> handler, boolean deleteAccount);
	void deleteAccount();
}