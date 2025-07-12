package org.nodex.android.account;
import org.nodex.android.dontkillmelib.DozeHelper;
import org.nodex.android.dontkillmelib.DozeHelperImpl;
import dagger.Module;
import dagger.Provides;
@Module
public class DozeHelperModule {
	@Provides
	DozeHelper provideDozeHelper() {
		return new DozeHelperImpl();
	}
}