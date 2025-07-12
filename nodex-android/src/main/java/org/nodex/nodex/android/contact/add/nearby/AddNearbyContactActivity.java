package org.nodex.android.contact.add.nearby;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.qrcode.QrCodeClassifier.QrCodeType;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.NodexActivity;
import org.nodex.android.contact.add.nearby.AddContactState.ContactExchangeFinished;
import org.nodex.android.contact.add.nearby.AddContactState.ContactExchangeResult;
import org.nodex.android.contact.add.nearby.AddContactState.Failed;
import org.nodex.android.contact.add.nearby.AddContactState.Failed.WrongQrCodeType;
import org.nodex.android.contact.add.nearby.AddContactState.Failed.WrongQrCodeVersion;
import org.nodex.android.contact.add.nearby.AddNearbyContactViewModel.BluetoothDecision;
import org.nodex.android.fragment.BaseFragment;
import org.nodex.android.fragment.BaseFragment.BaseFragmentListener;
import org.nodex.android.util.ActivityLaunchers.RequestBluetoothDiscoverable;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.api.qrcode.QrCodeClassifier.QrCodeType.MAILBOX;
import static org.nodex.android.contact.add.nearby.AddNearbyContactViewModel.BluetoothDecision.ACCEPTED;
import static org.nodex.android.contact.add.nearby.AddNearbyContactViewModel.BluetoothDecision.REFUSED;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class AddNearbyContactActivity extends NodexActivity
		implements BaseFragmentListener {
	private static final Logger LOG =
			getLogger(AddNearbyContactActivity.class.getName());
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private AddNearbyContactViewModel viewModel;
	private final ActivityResultLauncher<Integer> bluetoothLauncher =
			registerForActivityResult(new RequestBluetoothDiscoverable(),
					this::onBluetoothDiscoverableResult);
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(AddNearbyContactViewModel.class);
	}
	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_fragment_container_toolbar);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		if (state == null) {
			showInitialFragment(AddNearbyContactIntroFragment.newInstance());
		}
		viewModel.getRequestBluetoothDiscoverable().observeEvent(this, r ->
				requestBluetoothDiscoverable());
		viewModel.getShowQrCodeFragment().observeEvent(this, show -> {
			if (show) showQrCodeFragment();
		});
		requireNonNull(getSupportActionBar())
				.setTitle(R.string.add_contact_title);
		viewModel.getState()
				.observe(this, this::onAddContactStateChanged);
	}
	private void onBluetoothDiscoverableResult(boolean discoverable) {
		if (discoverable) {
			LOG.info("Bluetooth discoverability was accepted");
			viewModel.setBluetoothDecision(ACCEPTED);
		} else {
			LOG.info("Bluetooth discoverability was refused");
			viewModel.setBluetoothDecision(REFUSED);
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onBackPressed() {
		if (viewModel.getState().getValue() instanceof Failed) {
			Intent i = new Intent(this, AddNearbyContactActivity.class);
			i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		} else {
			super.onBackPressed();
		}
	}
	private void requestBluetoothDiscoverable() {
		Intent i = new Intent(ACTION_REQUEST_DISCOVERABLE);
		if (i.resolveActivity(getPackageManager()) != null) {
			LOG.info("Asking for Bluetooth discoverability");
			viewModel.setBluetoothDecision(BluetoothDecision.WAITING);
			bluetoothLauncher.launch(120);
		} else {
			viewModel.setBluetoothDecision(BluetoothDecision.NO_ADAPTER);
		}
	}
	private void showQrCodeFragment() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(AddNearbyContactFragment.TAG) == null) {
			BaseFragment f = AddNearbyContactFragment.newInstance();
			fm.beginTransaction()
					.replace(R.id.fragmentContainer, f, f.getUniqueTag())
					.addToBackStack(f.getUniqueTag())
					.commit();
		}
	}
	private void onAddContactStateChanged(@Nullable AddContactState state) {
		if (state instanceof ContactExchangeFinished) {
			ContactExchangeResult result =
					((ContactExchangeFinished) state).result;
			onContactExchangeResult(result);
		} else if (state instanceof WrongQrCodeType) {
			QrCodeType qrCodeType = ((WrongQrCodeType) state).qrCodeType;
			if (qrCodeType == MAILBOX) onMailboxQrCodeScanned();
			else onWrongQrCodeType();
		} else if (state instanceof WrongQrCodeVersion) {
			boolean qrCodeTooOld = ((WrongQrCodeVersion) state).qrCodeTooOld;
			onWrongQrCodeVersion(qrCodeTooOld);
		} else if (state instanceof Failed) {
			showErrorFragment();
		}
	}
	private void onContactExchangeResult(ContactExchangeResult result) {
		if (result instanceof ContactExchangeResult.Success) {
			Author remoteAuthor =
					((ContactExchangeResult.Success) result).remoteAuthor;
			String contactName = remoteAuthor.getName();
			String text = getString(R.string.contact_added_toast, contactName);
			Toast.makeText(this, text, LENGTH_LONG).show();
			supportFinishAfterTransition();
		} else if (result instanceof ContactExchangeResult.Error) {
			Author duplicateAuthor =
					((ContactExchangeResult.Error) result).duplicateAuthor;
			if (duplicateAuthor == null) {
				showErrorFragment();
			} else {
				String contactName = duplicateAuthor.getName();
				String text =
						getString(R.string.contact_already_exists, contactName);
				Toast.makeText(this, text, LENGTH_LONG).show();
				supportFinishAfterTransition();
			}
		} else throw new AssertionError();
	}
	private void onMailboxQrCodeScanned() {
		String title = getString(R.string.qr_code_invalid);
		String msg = getString(R.string.mailbox_qr_code_for_contact);
		showNextFragment(
				AddNearbyContactErrorFragment.newInstance(title, msg, false));
	}
	private void onWrongQrCodeType() {
		String title = getString(R.string.qr_code_invalid);
		String msg = getString(R.string.qr_code_format_unknown);
		showNextFragment(
				AddNearbyContactErrorFragment.newInstance(title, msg, false));
	}
	private void onWrongQrCodeVersion(boolean qrCodeTooOld) {
		String title = getString(R.string.qr_code_invalid);
		String msg;
		if (qrCodeTooOld) msg = getString(R.string.qr_code_too_old_1);
		else msg = getString(R.string.qr_code_too_new_1);
		showNextFragment(
				AddNearbyContactErrorFragment.newInstance(title, msg, false));
	}
	private void showErrorFragment() {
		showNextFragment(new AddNearbyContactErrorFragment());
	}
}