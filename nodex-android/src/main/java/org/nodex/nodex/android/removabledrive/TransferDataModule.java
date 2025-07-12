package org.nodex.android.removabledrive;
import org.nodex.android.viewmodel.ViewModelKey;
import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
@Module
public interface TransferDataModule {
	@Binds
	@IntoMap
	@ViewModelKey(RemovableDriveViewModel.class)
	ViewModel bindRemovableDriveViewModel(
			RemovableDriveViewModel removableDriveViewModel);
}