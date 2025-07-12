package org.nodex.android.activity;
import android.app.Activity;
import org.nodex.android.controller.NodexController;
import org.nodex.android.controller.NodexControllerImpl;
import org.nodex.android.controller.DbController;
import org.nodex.android.controller.DbControllerImpl;
import dagger.Module;
import dagger.Provides;
import static org.nodex.android.NodexService.NodexServiceConnection;
@Module
public class ActivityModule {
	private final BaseActivity activity;
	public ActivityModule(BaseActivity activity) {
		this.activity = activity;
	}
	@ActivityScope
	@Provides
	BaseActivity provideBaseActivity() {
		return activity;
	}
	@ActivityScope
	@Provides
	Activity provideActivity() {
		return activity;
	}
	@ActivityScope
	@Provides
	protected NodexController provideBriarController(
			NodexControllerImpl briarController) {
		activity.addLifecycleController(briarController);
		return briarController;
	}
	@ActivityScope
	@Provides
	DbController provideDBController(DbControllerImpl dbController) {
		return dbController;
	}
	@ActivityScope
	@Provides
	NodexServiceConnection provideBriarServiceConnection() {
		return new NodexServiceConnection();
	}
}