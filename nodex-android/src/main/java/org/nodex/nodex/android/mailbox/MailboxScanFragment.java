package org.nodex.android.mailbox;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import org.nodex.R;
import org.nodex.android.qrcode.CameraException;
import org.nodex.android.qrcode.CameraView;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.android.AppModule.getAndroidComponent;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class MailboxScanFragment extends Fragment {
	static final String TAG = MailboxScanFragment.class.getName();
	private static final Logger LOG = Logger.getLogger(TAG);
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private MailboxViewModel viewModel;
	private CameraView cameraView;
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		FragmentActivity activity = requireActivity();
		getAndroidComponent(activity).inject(this);
		viewModel = new ViewModelProvider(activity, viewModelFactory)
				.get(MailboxViewModel.class);
	}
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mailbox_scan, container,
				false);
	}
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		cameraView = view.findViewById(R.id.camera_view);
		cameraView.setPreviewConsumer(viewModel.getQrCodeDecoder());
	}
	@Override
	public void onStart() {
		super.onStart();
		requireActivity().setTitle(R.string.mailbox_setup_button_scan);
		try {
			cameraView.start();
		} catch (CameraException e) {
			logCameraExceptionAndFinish(e);
		}
	}
	@Override
	public void onStop() {
		super.onStop();
		try {
			cameraView.stop();
		} catch (CameraException e) {
			logCameraExceptionAndFinish(e);
		}
	}
	@UiThread
	private void logCameraExceptionAndFinish(CameraException e) {
		logException(LOG, WARNING, e);
		Toast.makeText(requireContext(), R.string.camera_error,
				LENGTH_LONG).show();
		viewModel.onCameraError();
	}
}