package org.nodex.android.contact.add.nearby;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.fragment.BaseFragment;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions;
import androidx.lifecycle.ViewModelProvider;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class AddNearbyContactIntroFragment extends BaseFragment {
	public static final String TAG =
			AddNearbyContactIntroFragment.class.getName();
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private AddNearbyContactViewModel viewModel;
	private AddNearbyContactPermissionManager permissionManager;
	private final ActivityResultLauncher<String[]> permissionLauncher =
			registerForActivityResult(new RequestMultiplePermissions(), r -> {
				permissionManager.onRequestPermissionResult(r);
				if (permissionManager.checkPermissions()) {
					viewModel.showQrCodeFragmentIfAllowed();
				}
			});
	public static AddNearbyContactIntroFragment newInstance() {
		Bundle args = new Bundle();
		AddNearbyContactIntroFragment
				fragment = new AddNearbyContactIntroFragment();
		fragment.setArguments(args);
		return fragment;
	}
	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
				.get(AddNearbyContactViewModel.class);
		permissionManager = new AddNearbyContactPermissionManager(
				requireActivity(), permissionLauncher::launch,
				viewModel.isBluetoothSupported());
	}
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_keyagreement_id, container,
				false);
		View button = v.findViewById(R.id.continueButton);
		button.setOnClickListener(view -> {
			viewModel.stopDiscovery();
			viewModel.onContinueClicked();
			if (permissionManager.checkPermissions()) {
				viewModel.showQrCodeFragmentIfAllowed();
			}
		});
		return v;
	}
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel.stopListening();
	}
	@Override
	public void onStart() {
		super.onStart();
		permissionManager.resetPermissions();
		viewModel.resetPlugins();
	}
	@Override
	public String getUniqueTag() {
		return TAG;
	}
}