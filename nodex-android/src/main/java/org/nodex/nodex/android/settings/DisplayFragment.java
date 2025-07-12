package org.nodex.android.settings;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.nodex.R;
import org.nodex.android.Localizer;
import org.nodex.android.util.UiUtils;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static java.util.Objects.requireNonNull;
import static org.nodex.android.NodexApplication.ENTRY_ACTIVITY;
import static org.nodex.android.navdrawer.NavDrawerActivity.SIGN_OUT_URI;
import static org.nodex.android.settings.SettingsActivity.EXTRA_THEME_CHANGE;
@NotNullByDefault
public class DisplayFragment extends PreferenceFragmentCompat {
	public static final String PREF_LANGUAGE = "pref_key_language";
	public static final String PREF_THEME = "pref_key_theme";
	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.settings_display);
		ListPreference language = requireNonNull(findPreference(PREF_LANGUAGE));
		setLanguageEntries(language);
		language.setOnPreferenceChangeListener(this::onLanguageChanged);
		ListPreference theme = requireNonNull(findPreference(PREF_THEME));
		theme.setOnPreferenceChangeListener(this::onThemeChanged);
	}
	@Override
	public void onStart() {
		super.onStart();
		requireActivity().setTitle(R.string.display_settings_title);
	}
	private void setLanguageEntries(ListPreference language) {
		CharSequence[] tags = language.getEntryValues();
		List<CharSequence> entries = new ArrayList<>(tags.length);
		List<CharSequence> entryValues = new ArrayList<>(tags.length);
		for (CharSequence cs : tags) {
			String tag = cs.toString();
			if (tag.equals("default")) {
				entries.add(getString(R.string.pref_language_default));
				entryValues.add(tag);
				continue;
			}
			Locale locale = Localizer.getLocaleFromTag(tag);
			if (locale == null)
				throw new IllegalStateException();
			String nativeName = locale.getDisplayName(locale);
			if (nativeName.equals(tag)) {
				String tmp = locale.getDisplayLanguage(Locale.ENGLISH);
				if (!tmp.isEmpty() && !tmp.equals(nativeName))
					nativeName = tmp;
			}
			entries.add("\u200E" + nativeName.substring(0, 1).toUpperCase()
					+ nativeName.substring(1));
			entryValues.add(tag);
		}
		language.setEntries(entries.toArray(new CharSequence[0]));
		language.setEntryValues(entryValues.toArray(new CharSequence[0]));
	}
	private boolean onThemeChanged(Preference preference, Object newValue) {
		FragmentActivity activity = requireActivity();
		UiUtils.setTheme(activity, (String) newValue);
		Intent intent = new Intent(getActivity(), ENTRY_ACTIVITY);
		intent.setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		intent = new Intent(getActivity(), activity.getClass());
		intent.putExtra(EXTRA_THEME_CHANGE, true);
		startActivity(intent);
		activity.finish();
		return true;
	}
	private boolean onLanguageChanged(Preference preference, Object newValue) {
		ListPreference language = (ListPreference) preference;
		if (!language.getValue().equals(newValue)) {
			MaterialAlertDialogBuilder builder =
					new MaterialAlertDialogBuilder(requireContext());
			builder.setTitle(R.string.pref_language_title);
			builder.setMessage(R.string.pref_language_changed);
			builder.setPositiveButton(R.string.sign_out_button, (d, i) -> {
				language.setValue((String) newValue);
				Intent intent = new Intent(getContext(), ENTRY_ACTIVITY);
				intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
				intent.setData(SIGN_OUT_URI);
				requireActivity().startActivity(intent);
				requireActivity().finish();
			});
			builder.setNegativeButton(R.string.cancel, null);
			builder.setCancelable(false);
			builder.show();
		}
		return false;
	}
}