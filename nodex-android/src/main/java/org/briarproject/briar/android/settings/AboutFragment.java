package org.nodex.android.settings;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.nodex.BuildConfig;
import org.nodex.R;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.logging.Logger;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import static android.content.Intent.ACTION_VIEW;
import static java.util.logging.Logger.getLogger;
import static org.nodex.android.util.UiUtils.tryToStartActivity;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class AboutFragment extends Fragment {
	final static String TAG = AboutFragment.class.getName();
	private static final Logger LOG = getLogger(TAG);
	private TextView briarVersion;
	private TextView torVersion;
	private TextView briarWebsite;
	private TextView briarSourceCode;
	private TextView briarChangelog;
	private TextView briarPrivacyPolicy;
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_about, container,
				false);
	}
	@Override
	public void onStart() {
		super.onStart();
		requireActivity().setTitle(R.string.about_title);
		briarVersion = requireActivity().findViewById(R.id.BriarVersion);
		briarVersion.setText(
				getString(R.string.briar_version, BuildConfig.VERSION_NAME));
		torVersion = requireActivity().findViewById(R.id.TorVersion);
		torVersion.setText(
				getString(R.string.tor_version, BuildConfig.TorVersion));
		briarWebsite = requireActivity().findViewById(R.id.BriarWebsite);
		briarSourceCode = requireActivity().findViewById(R.id.BriarSourceCode);
		briarChangelog = requireActivity().findViewById(R.id.BriarChangelog);
		briarPrivacyPolicy =
				requireActivity().findViewById(R.id.BriarPrivacyPolicy);
		briarWebsite.setOnClickListener(View -> {
			String url = "https:
			goToUrl(url);
		});
		briarSourceCode.setOnClickListener(View -> {
			String url = "https:
			goToUrl(url);
		});
		briarChangelog.setOnClickListener(View -> {
			String url =
					"https:
			goToUrl(url);
		});
		briarPrivacyPolicy.setOnClickListener(View -> {
			String url =
					"https:
			goToUrl(url);
		});
	}
	private void goToUrl(String url) {
		Intent i = new Intent(ACTION_VIEW);
		i.setData(Uri.parse(url));
		tryToStartActivity(requireActivity(), i);
	}
}