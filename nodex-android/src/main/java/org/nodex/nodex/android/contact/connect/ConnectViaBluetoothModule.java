package org.nodex.android.contact.connect;
import org.nodex.android.viewmodel.ViewModelKey;
import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
@Module
public abstract class ConnectViaBluetoothModule {
	@Binds
	@IntoMap
	@ViewModelKey(ConnectViaBluetoothViewModel.class)
	abstract ViewModel bindContactListViewModel(
			ConnectViaBluetoothViewModel connectViaBluetoothViewModel);
}