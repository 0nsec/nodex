package org.nodex.android.mailbox;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.nodex.core.api.mailbox.MailboxPairingState;
import org.nodex.core.api.mailbox.MailboxPairingState.ConnectionError;
import org.nodex.core.api.mailbox.MailboxPairingState.InvalidQrCode;
import org.nodex.core.api.mailbox.MailboxPairingState.MailboxAlreadyPaired;
import org.nodex.core.api.mailbox.MailboxPairingState.Paired;
import org.nodex.core.api.mailbox.MailboxPairingState.Pending;
import org.nodex.core.api.mailbox.MailboxPairingState.UnexpectedError;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.NodexActivity;
import org.nodex.android.fragment.FinalFragment;
import org.nodex.android.mailbox.MailboxState.CameraError;
import org.nodex.android.mailbox.MailboxState.IsPaired;
import org.nodex.android.mailbox.MailboxState.NotSetup;
import org.nodex.android.mailbox.MailboxState.OfflineWhenPairing;
import org.nodex.android.mailbox.MailboxState.Pairing;
import org.nodex.android.mailbox.MailboxState.ScanningQrCode;
import org.nodex.android.mailbox.MailboxState.ShowDownload;
import org.nodex.android.mailbox.MailboxState.WasUnpaired;
import org.nodex.android.view.BlankFragment;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static org.nodex.core.api.mailbox.MailboxConstants.QR_FORMAT_VERSION;
import static org.nodex.core.api.qrcode.QrCodeClassifier.QrCodeType.BQP;
import static org.nodex.core.api.qrcode.QrCodeClassifier.QrCodeType.MAILBOX;
import static org.nodex.android.util.UiUtils.showFragment;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class MailboxActivity extends NodexActivity {
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private MailboxViewModel viewModel;
	private ProgressBar progressBar;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(MailboxViewModel.class);
	}
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mailbox);
		progressBar = findViewById(R.id.progressBar);
		if (viewModel.getPairingState().getValue() == null) {
			progressBar.setVisibility(VISIBLE);
		}
		viewModel.getPairingState().observeEvent(this, state -> {
			if (state instanceof NotSetup) {
				onNotSetup();
			} else if (state instanceof ShowDownload) {
				onShowDownload();
			} else if (state instanceof ScanningQrCode) {
				onScanningQrCode();
			} else if (state instanceof Pairing) {
				MailboxPairingState s = ((Pairing) state).pairingState;
				onMailboxPairingStateChanged(s);
			} else if (state instanceof OfflineWhenPairing) {
				onOffline();
			} else if (state instanceof CameraError) {
				onCameraError();
			} else if (state instanceof IsPaired) {
				onIsPaired(((IsPaired) state).isOnline);
			} else if (state instanceof WasUnpaired) {
				WasUnpaired s = (WasUnpaired) state;
				onUnPaired(s.tellUserToWipeMailbox);
			} else {
				throw new AssertionError("Unknown state: " + state);
			}
		});
		if (savedInstanceState != null) {
			FragmentManager fm = getSupportFragmentManager();
			Fragment f = fm.findFragmentByTag(BlankFragment.TAG);
			if (f != null && f.isAdded()) {
				onUnPaired(true);
			}
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
		MailboxState s = viewModel.getPairingState().getLastValue();
		if (s instanceof Pairing) {
			supportFinishAfterTransition();
		} else {
			super.onBackPressed();
		}
	}
	private void onNotSetup() {
		progressBar.setVisibility(INVISIBLE);
		FragmentManager fm = getSupportFragmentManager();
		if (fm.getBackStackEntryCount() == 0) {
			showFragment(fm, new SetupIntroFragment(), SetupIntroFragment.TAG,
					false);
		}
	}
	private void onShowDownload() {
		boolean needToShow = true;
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(SetupDownloadFragment.TAG) != null) {
			needToShow =
					!fm.popBackStackImmediate(SetupDownloadFragment.TAG, 0);
		}
		if (needToShow) {
			showFragment(fm, new SetupDownloadFragment(),
					SetupDownloadFragment.TAG);
		}
	}
	private void onScanningQrCode() {
		showFragment(getSupportFragmentManager(), new MailboxScanFragment(),
				MailboxScanFragment.TAG);
	}
	private void onMailboxPairingStateChanged(MailboxPairingState s) {
		progressBar.setVisibility(INVISIBLE);
		FragmentManager fm = getSupportFragmentManager();
		if (fm.getBackStackEntryCount() == 0) {
			onNotSetup();
			onShowDownload();
		}
		Fragment f;
		String tag;
		if (s instanceof Pending) {
			long timeStarted = ((Pending) s).timeStarted;
			f = MailboxConnectingFragment.newInstance(timeStarted);
			tag = MailboxConnectingFragment.TAG;
		} else if (s instanceof InvalidQrCode) {
			InvalidQrCode i = (InvalidQrCode) s;
			int errorRes;
			if (i.qrCodeType == MAILBOX) {
				if (i.formatVersion < QR_FORMAT_VERSION) {
					errorRes = R.string.mailbox_qr_code_too_old;
				} else if (i.formatVersion > QR_FORMAT_VERSION) {
					errorRes = R.string.mailbox_qr_code_too_new;
				} else {
					errorRes = R.string.mailbox_setup_qr_code_wrong_description;
				}
			} else if (i.qrCodeType == BQP) {
				errorRes = R.string.contact_qr_code_for_mailbox;
			} else {
				errorRes = R.string.mailbox_setup_qr_code_wrong_description;
			}
			f = ErrorFragment.newInstance(R.string.qr_code_invalid, errorRes);
			tag = ErrorFragment.TAG;
		} else if (s instanceof MailboxAlreadyPaired) {
			f = ErrorFragment.newInstance(
					R.string.mailbox_setup_already_paired_title,
					R.string.mailbox_setup_already_paired_description);
			tag = ErrorFragment.TAG;
		} else if (s instanceof ConnectionError) {
			f = ErrorFragment.newInstance(
					R.string.mailbox_setup_io_error_title,
					R.string.mailbox_setup_io_error_description);
			tag = ErrorFragment.TAG;
		} else if (s instanceof UnexpectedError) {
			f = ErrorFragment.newInstance(
					R.string.mailbox_setup_assertion_error_title,
					R.string.mailbox_setup_assertion_error_description);
			tag = ErrorFragment.TAG;
		} else if (s instanceof Paired) {
			f = FinalFragment.newInstance(R.string.mailbox_setup_paired_title,
					R.drawable.ic_check_circle_outline,
					R.color.briar_brand_green,
					R.string.mailbox_setup_paired_description);
			tag = FinalFragment.TAG;
		} else {
			throw new IllegalStateException("Unhandled state: " + s.getClass());
		}
		showFragment(fm, f, tag);
	}
	private void onOffline() {
		showFragment(getSupportFragmentManager(), new OfflineFragment(),
				OfflineFragment.TAG);
	}
	private void onCameraError() {
		Fragment f = ErrorFragment.newInstance(
				R.string.mailbox_setup_camera_error_title,
				R.string.mailbox_setup_camera_error_description);
		showFragment(getSupportFragmentManager(), f, ErrorFragment.TAG);
	}
	private void onIsPaired(boolean isOnline) {
		progressBar.setVisibility(INVISIBLE);
		Fragment f = isOnline ?
				new MailboxStatusFragment() : new OfflineStatusFragment();
		String tag = isOnline ?
				MailboxStatusFragment.TAG : OfflineStatusFragment.TAG;
		showFragment(getSupportFragmentManager(), f, tag, false);
	}
	private void onUnPaired(boolean tellUserToWipeMailbox) {
		viewModel.clearProblemNotification();
		if (tellUserToWipeMailbox) {
			showFragment(getSupportFragmentManager(), new BlankFragment(),
					BlankFragment.TAG);
			MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
					this, R.style.NodexDialogTheme);
			builder.setTitle(R.string.mailbox_status_unlink_no_wipe_title);
			builder.setMessage(R.string.mailbox_status_unlink_no_wipe_message);
			builder.setNeutralButton(R.string.got_it,
					(dialog, which) -> dialog.cancel());
			builder.setOnCancelListener(
					dialog -> supportFinishAfterTransition());
			builder.show();
		} else {
			Toast.makeText(this, R.string.mailbox_status_unlink_success,
					LENGTH_LONG).show();
			supportFinishAfterTransition();
		}
	}
}