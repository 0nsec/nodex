package org.nodex.android.settings;
import android.content.Context;
import org.nodex.R;
import org.nodex.nullsafety.NotNullByDefault;
import org.nodex.onionwrapper.CircumventionProvider;
import org.nodex.onionwrapper.LocationUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference.SummaryProvider;
import static org.nodex.core.api.plugin.TorConstants.PREF_TOR_NETWORK_AUTOMATIC;
import static org.nodex.android.util.UiUtils.getCountryDisplayName;
@NotNullByDefault
class TorSummaryProvider implements SummaryProvider<ListPreference> {
	private final Context ctx;
	private final LocationUtils locationUtils;
	private final CircumventionProvider circumventionProvider;
	TorSummaryProvider(Context ctx,
			LocationUtils locationUtils,
			CircumventionProvider circumventionProvider) {
		this.ctx = ctx;
		this.locationUtils = locationUtils;
		this.circumventionProvider = circumventionProvider;
	}
	@Override
	public CharSequence provideSummary(ListPreference preference) {
		int torNetworkSetting = Integer.parseInt(preference.getValue());
		if (torNetworkSetting != PREF_TOR_NETWORK_AUTOMATIC) {
			return preference.getEntry();
		}
		String country = locationUtils.getCurrentCountry();
		String countryName = getCountryDisplayName(country);
		boolean useBridgesByDefault =
				circumventionProvider.shouldUseBridges(country);
		String setting =
				ctx.getString(R.string.tor_network_setting_without_bridges);
		if (useBridgesByDefault) {
			setting = ctx.getString(R.string.tor_network_setting_with_bridges);
		}
		return ctx.getString(R.string.tor_network_setting_summary, setting,
				countryName);
	}
}