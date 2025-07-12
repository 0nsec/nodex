package org.nodex.android.panic;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.nodex.R;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import info.guardianproject.panic.PanicResponder;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static info.guardianproject.panic.Panic.PACKAGE_NAME_NONE;
public class PanicPreferencesFragment extends PreferenceFragmentCompat
		implements SharedPreferences.OnSharedPreferenceChangeListener {
	public static final String KEY_LOCK = "pref_key_lock";
	public static final String KEY_PANIC_APP = "pref_key_panic_app";
	public static final String KEY_PURGE = "pref_key_purge";
	private static final Logger LOG =
			Logger.getLogger(PanicPreferencesFragment.class.getName());
	private PackageManager pm;
	private SwitchPreferenceCompat lockPref, purgePref;
	private ListPreference panicAppPref;
	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.panic_preferences);
	}
	private void updatePreferences() {
		pm = getActivity().getPackageManager();
		lockPref = findPreference(KEY_LOCK);
		panicAppPref = findPreference(KEY_PANIC_APP);
		purgePref = findPreference(KEY_PURGE);
		if (PanicResponder.checkForDisconnectIntent(getActivity())) {
			LOG.info("Received DISCONNECT intent from Panic Trigger App.");
			getActivity().finish();
		} else {
			String packageName =
					PanicResponder.getConnectIntentSender(getActivity());
			if (!TextUtils.isEmpty((packageName)) &&
					!TextUtils.equals(packageName,
							PanicResponder
									.getTriggerPackageName(getActivity()))) {
				LOG.info("Received CONNECT intent from new Panic Trigger App.");
				showOptInDialog();
			}
		}
		ArrayList<CharSequence> entries = new ArrayList<>();
		ArrayList<CharSequence> entryValues = new ArrayList<>();
		entries.add(0, getString(R.string.panic_app_setting_none));
		entryValues.add(0, PACKAGE_NAME_NONE);
		for (ResolveInfo resolveInfo : PanicResponder.resolveTriggerApps(pm)) {
			if (resolveInfo.activityInfo == null)
				continue;
			entries.add(resolveInfo.activityInfo.loadLabel(pm));
			entryValues.add(resolveInfo.activityInfo.packageName);
		}
		panicAppPref.setEntries(entries.toArray(new CharSequence[0]));
		panicAppPref.setEntryValues(entryValues.toArray(new CharSequence[0]));
		panicAppPref.setDefaultValue(PACKAGE_NAME_NONE);
		panicAppPref.setOnPreferenceChangeListener((preference, newValue) -> {
			String packageName = (String) newValue;
			PanicResponder.setTriggerPackageName(getActivity(), packageName);
			showPanicApp(packageName);
			if (packageName.equals(PACKAGE_NAME_NONE)) {
				purgePref.setChecked(false);
				purgePref.setEnabled(false);
				getActivity().setResult(RESULT_CANCELED);
			} else {
				purgePref.setEnabled(true);
			}
			return true;
		});
		if (entries.size() <= 1) {
			panicAppPref.setOnPreferenceClickListener(preference -> {
				Intent intent = new Intent(ACTION_VIEW);
				intent.setData(Uri.parse(
						"market:
				intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
				if (intent.resolveActivity(getActivity().getPackageManager())
						!= null) {
					startActivity(intent);
				}
				return true;
			});
		} else {
			panicAppPref.setOnPreferenceClickListener(null);
		}
	}
	@Override
	public void onStart() {
		super.onStart();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		updatePreferences();
		showPanicApp(PanicResponder.getTriggerPackageName(getActivity()));
	}
	@Override
	public void onStop() {
		super.onStop();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(KEY_PURGE) &&
				sharedPreferences.getBoolean(KEY_PURGE, false)) {
			lockPref.setChecked(true);
		}
		if (key.equals(KEY_LOCK) &&
				!sharedPreferences.getBoolean(KEY_LOCK, true)) {
			purgePref.setChecked(false);
		}
	}
	private void showPanicApp(String triggerPackageName) {
		if (TextUtils.isEmpty(triggerPackageName)
				|| triggerPackageName.equals(PACKAGE_NAME_NONE)) {
			panicAppPref.setValue(PACKAGE_NAME_NONE);
			panicAppPref
					.setSummary(getString(R.string.panic_app_setting_summary));
			panicAppPref.setIcon(
					android.R.drawable.ic_menu_close_clear_cancel);
			purgePref.setEnabled(false);
		} else {
			try {
				panicAppPref.setValue(triggerPackageName);
				panicAppPref.setSummary(pm.getApplicationLabel(
						pm.getApplicationInfo(triggerPackageName, 0)));
				panicAppPref.setIcon(
						pm.getApplicationIcon(triggerPackageName));
				purgePref.setEnabled(true);
			} catch (PackageManager.NameNotFoundException e) {
				PanicResponder.setTriggerPackageName(getActivity(),
						PACKAGE_NAME_NONE);
				showPanicApp(PACKAGE_NAME_NONE);
			}
		}
	}
	private void showOptInDialog() {
		DialogInterface.OnClickListener okListener = (dialog, which) -> {
			PanicResponder.setTriggerPackageName(getActivity());
			showPanicApp(PanicResponder.getTriggerPackageName(getActivity()));
			getActivity().setResult(RESULT_OK);
		};
		DialogInterface.OnClickListener cancelListener = (dialog, which) -> {
			getActivity().setResult(RESULT_CANCELED);
			getActivity().finish();
		};
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
				requireContext(), R.style.BriarDialogTheme);
		builder.setTitle(getString(R.string.dialog_title_connect_panic_app));
		CharSequence app = getString(R.string.unknown_app);
		String packageName = getCallingPackageName();
		if (packageName != null) {
			try {
				app = pm.getApplicationLabel(
						pm.getApplicationInfo(packageName, 0));
			} catch (PackageManager.NameNotFoundException e) {
				LOG.warning(e.toString());
			}
		}
		String text = String.format(
				getString(R.string.dialog_message_connect_panic_app), app);
		builder.setMessage(text);
		builder.setNegativeButton(R.string.allow, okListener);
		builder.setPositiveButton(R.string.cancel, cancelListener);
		builder.show();
	}
	@Nullable
	private String getCallingPackageName() {
		ComponentName componentName = getActivity().getCallingActivity();
		String packageName = null;
		if (componentName != null) {
			packageName = componentName.getPackageName();
		}
		return packageName;
	}
}