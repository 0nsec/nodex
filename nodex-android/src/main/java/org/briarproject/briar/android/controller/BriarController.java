package org.briarproject.briar.android.controller;
import org.briarproject.bramble.api.system.Wakeful;
import org.briarproject.briar.android.controller.handler.ResultHandler;
import org.briarproject.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface BriarController extends ActivityLifecycleController {
	void startAndBindService();
	boolean accountSignedIn();
	void hasDozed(ResultHandler<Boolean> handler);
	void doNotAskAgainForDozeWhiteListing();
	@Wakeful
	void signOut(ResultHandler<Void> handler, boolean deleteAccount);
	void deleteAccount();
}