package org.nodex.android.login;
import org.nodex.android.viewmodel.ViewModelKey;
import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
@Module
public abstract class LoginModule {
	@Binds
	@IntoMap
	@ViewModelKey(StartupViewModel.class)
	abstract ViewModel bindStartupViewModel(StartupViewModel viewModel);
	@Binds
	@IntoMap
	@ViewModelKey(ChangePasswordViewModel.class)
	abstract ViewModel bindChangePasswordViewModel(
			ChangePasswordViewModel viewModel);
}