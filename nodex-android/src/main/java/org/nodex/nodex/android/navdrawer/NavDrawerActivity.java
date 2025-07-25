package org.nodex.android.navdrawer;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.plugin.BluetoothConstants;
import org.nodex.core.api.plugin.LanTcpConstants;
import org.nodex.core.api.plugin.Plugin.State;
import org.nodex.core.api.plugin.TorConstants;
import org.nodex.core.api.plugin.TransportId;
import org.nodex.R;
import org.nodex.android.NodexApplication;
import org.nodex.android.StartupFailureActivity;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.NodexActivity;
import org.nodex.android.blog.FeedFragment;
import org.nodex.android.contact.ContactListFragment;
import org.nodex.android.forum.ForumListFragment;
import org.nodex.android.fragment.BaseFragment;
import org.nodex.android.fragment.BaseFragment.BaseFragmentListener;
import org.nodex.android.logout.SignOutFragment;
import org.nodex.android.privategroup.list.GroupListFragment;
import org.nodex.android.settings.SettingsActivity;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.core.view.GravityCompat.START;
import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.api.lifecycle.LifecycleManager.LifecycleState.RUNNING;
import static org.nodex.core.api.plugin.Plugin.State.ACTIVE;
import static org.nodex.core.api.plugin.Plugin.State.ENABLING;
import static org.nodex.core.api.plugin.Plugin.State.STARTING_STOPPING;
import static org.nodex.android.NodexService.EXTRA_STARTUP_FAILED;
import static org.nodex.android.NodexService.EXTRA_START_RESULT;
import static org.nodex.android.TestingConstants.IS_DEBUG_BUILD;
import static org.nodex.android.activity.RequestCodes.REQUEST_PASSWORD;
import static org.nodex.android.navdrawer.IntentRouter.handleExternalIntent;
import static org.nodex.android.util.UiUtils.getDaysUntilExpiry;
import static org.nodex.android.util.UiUtils.observeOnce;
import static org.nodex.android.util.UiUtils.resolveColorAttribute;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class NavDrawerActivity extends NodexActivity implements
		BaseFragmentListener, OnNavigationItemSelectedListener {
	private static final Logger LOG =
			getLogger(NavDrawerActivity.class.getName());
	public static Uri CONTACT_URI =
			Uri.parse("nodex-content:
	public static Uri GROUP_URI =
			Uri.parse("nodex-content:
	public static Uri FORUM_URI =
			Uri.parse("nodex-content:
	public static Uri BLOG_URI =
			Uri.parse("nodex-content:
	public static Uri CONTACT_ADDED_URI =
			Uri.parse("nodex-content:
	public static Uri SIGN_OUT_URI =
			Uri.parse("nodex-content:
	private final List<Transport> transports = new ArrayList<>(3);
	private final MutableLiveData<ImageView> torIcon = new MutableLiveData<>();
	private NavDrawerViewModel navDrawerViewModel;
	private PluginViewModel pluginViewModel;
	private ActionBarDrawerToggle drawerToggle;
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	@Inject
	LifecycleManager lifecycleManager;
	private DrawerLayout drawerLayout;
	private NavigationView navigation;
	private BaseAdapter transportsAdapter;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		ViewModelProvider provider =
				new ViewModelProvider(this, viewModelFactory);
		navDrawerViewModel = provider.get(NavDrawerViewModel.class);
		pluginViewModel = provider.get(PluginViewModel.class);
	}
	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		exitIfStartupFailed(getIntent());
		setContentView(R.layout.activity_nav_drawer);
		NodexApplication app = (NodexApplication) getApplication();
		if (IS_DEBUG_BUILD && !app.isInstrumentationTest()) {
			navDrawerViewModel.showExpiryWarning()
					.observe(this, this::showExpiryWarning);
		}
		navDrawerViewModel.shouldAskForDozeWhitelisting().observe(this, ask -> {
			if (ask) showDozeDialog(R.string.dnkm_doze_intro);
		});
		Toolbar toolbar = setUpCustomToolbar(false);
		drawerLayout = findViewById(R.id.drawer_layout);
		navigation = findViewById(R.id.navigation);
		GridView transportsView = findViewById(R.id.transportsView);
		LinearLayout transportsLayout = findViewById(R.id.transports);
		transportsLayout.setOnClickListener(v -> {
			LOG.info("Starting transports activity");
			startActivity(new Intent(this, TransportsActivity.class));
		});
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
				R.string.nav_drawer_open_description,
				R.string.nav_drawer_close_description) {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				navDrawerViewModel.checkTransportsOnboarding();
			}
		};
		drawerLayout.addDrawerListener(drawerToggle);
		navigation.setNavigationItemSelectedListener(this);
		drawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						drawerLayout.getViewTreeObserver()
								.removeOnGlobalLayoutListener(this);
						observeTransportsOnboarding();
					}
				});
		initializeTransports();
		transportsView.setAdapter(transportsAdapter);
		lockManager.isLockable().observe(this, this::setLockVisible);
		if (lifecycleManager.getLifecycleState().isAfter(RUNNING)) {
			showSignOutFragment();
		}
		if (state == null) {
			onNewIntent(getIntent());
		}
	}
	private void observeTransportsOnboarding() {
		observeOnce(navDrawerViewModel.showTransportsOnboarding(), this,
				show -> {
					if (show) {
						observeOnce(torIcon, this,
								this::showTransportsOnboarding);
					}
				});
	}
	@Override
	public void onStart() {
		super.onStart();
		lockManager.checkIfLockable();
		if (IS_DEBUG_BUILD) {
			navDrawerViewModel.checkExpiryWarning();
		}
	}
	@Override
	protected void onActivityResult(int request, int result,
			@Nullable Intent data) {
		super.onActivityResult(request, result, data);
		if (request == REQUEST_PASSWORD && result == RESULT_OK) {
			navDrawerViewModel.checkDozeWhitelisting();
		}
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		exitIfStartupFailed(intent);
		if ("nodex-content".equals(intent.getScheme())) {
			handleContentIntent(intent);
		} else {
			handleExternalIntent(this, intent);
		}
	}
	private void handleContentIntent(Intent intent) {
		Uri uri = intent.getData();
		if (CONTACT_URI.equals(uri) || CONTACT_ADDED_URI.equals(uri)) {
			startFragment(ContactListFragment.newInstance(),
					R.id.nav_btn_contacts);
		} else if (GROUP_URI.equals(uri)) {
			startFragment(GroupListFragment.newInstance(), R.id.nav_btn_groups);
		} else if (FORUM_URI.equals(uri)) {
			startFragment(ForumListFragment.newInstance(), R.id.nav_btn_forums);
		} else if (BLOG_URI.equals(uri)) {
			startFragment(FeedFragment.newInstance(), R.id.nav_btn_blogs);
		} else if (SIGN_OUT_URI.equals(uri)) {
			signOut(false, false);
		}
	}
	private void exitIfStartupFailed(Intent intent) {
		if (intent.getBooleanExtra(EXTRA_STARTUP_FAILED, false)) {
			Intent i = new Intent(this, StartupFailureActivity.class);
			i.putExtra(EXTRA_START_RESULT,
					intent.getSerializableExtra(EXTRA_START_RESULT));
			startActivity(i);
			finish();
			LOG.info("Exiting");
			System.exit(0);
		}
	}
	private void loadFragment(int fragmentId) {
		if (fragmentId == R.id.nav_btn_contacts) {
			startFragment(ContactListFragment.newInstance());
		} else if (fragmentId == R.id.nav_btn_groups) {
			startFragment(GroupListFragment.newInstance());
		} else if (fragmentId == R.id.nav_btn_forums) {
			startFragment(ForumListFragment.newInstance());
		} else if (fragmentId == R.id.nav_btn_blogs) {
			startFragment(FeedFragment.newInstance());
		} else if (fragmentId == R.id.nav_btn_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
		} else if (fragmentId == R.id.nav_btn_signout) {
			signOut();
		}
	}
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		drawerLayout.closeDrawer(START);
		if (item.getItemId() == R.id.nav_btn_lock) {
			lockManager.setLocked(true);
			ActivityCompat.finishAfterTransition(this);
			return false;
		} else {
			loadFragment(item.getItemId());
			return item.getItemId() != R.id.nav_btn_settings;
		}
	}
	@Override
	public void onBackPressed() {
		if (drawerLayout.isDrawerOpen(START)) {
			drawerLayout.closeDrawer(START);
		} else {
			FragmentManager fm = getSupportFragmentManager();
			if (fm.findFragmentByTag(SignOutFragment.TAG) != null) {
				finish();
			} else if (fm.getBackStackEntryCount() == 0 &&
					fm.findFragmentByTag(ContactListFragment.TAG) == null) {
				if (!getLifecycle().getCurrentState().isAtLeast(STARTED)) {
					LOG.warning("Tried to start contacts fragment in state " +
							getLifecycle().getCurrentState().name());
					return;
				}
				startFragment(ContactListFragment.newInstance(),
						R.id.nav_btn_contacts);
			} else {
				super.onBackPressed();
			}
		}
	}
	@Override
	public void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}
	private void showSignOutFragment() {
		drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
		startFragment(new SignOutFragment());
	}
	private void signOut() {
		drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
		signOut(false, false);
		finish();
	}
	private void startFragment(BaseFragment fragment, int itemId) {
		navigation.setCheckedItem(itemId);
		startFragment(fragment);
	}
	private void startFragment(BaseFragment f) {
		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
						R.anim.fade_in, R.anim.fade_out)
				.replace(R.id.fragmentContainer, f, f.getUniqueTag())
				.commit();
	}
	@Override
	public void handleException(Exception e) {
	}
	private void setLockVisible(boolean visible) {
		MenuItem item = navigation.getMenu().findItem(R.id.nav_btn_lock);
		if (item != null) item.setVisible(visible);
	}
	private void showExpiryWarning(boolean show) {
		long daysUntilExpiry = getDaysUntilExpiry();
		if (daysUntilExpiry < 0) {
			signOut();
			return;
		}
		ViewGroup expiryWarning = findViewById(R.id.expiryWarning);
		if (show) {
			TextView expiryWarningText =
					expiryWarning.findViewById(R.id.expiryWarningText);
			String text = getResources().getQuantityString(
					R.plurals.expiry_warning, (int) daysUntilExpiry,
					(int) daysUntilExpiry);
			expiryWarningText.setText(text);
			ImageView expiryWarningClose =
					expiryWarning.findViewById(R.id.expiryWarningClose);
			expiryWarningClose.setOnClickListener(v ->
					navDrawerViewModel.expiryWarningDismissed());
			expiryWarning.setVisibility(VISIBLE);
		} else {
			expiryWarning.setVisibility(GONE);
		}
	}
	private void initializeTransports() {
		transportsAdapter = new BaseAdapter() {
			@Override
			public int getCount() {
				return transports.size();
			}
			@Override
			public Transport getItem(int position) {
				return transports.get(position);
			}
			@Override
			public long getItemId(int position) {
				return 0;
			}
			@Override
			public View getView(int position, @Nullable View convertView,
					ViewGroup parent) {
				View view;
				if (convertView != null) {
					view = convertView;
				} else {
					LayoutInflater inflater = getLayoutInflater();
					view = inflater.inflate(R.layout.list_item_transport,
							parent, false);
				}
				Transport t = getItem(position);
				ImageView icon = view.findViewById(R.id.imageView);
				icon.setImageResource(t.iconDrawable);
				icon.setColorFilter(ContextCompat.getColor(
						NavDrawerActivity.this, t.iconColor));
				TextView text = view.findViewById(R.id.textView);
				text.setText(getString(t.label));
				if (t.id.equals(TorConstants.ID)) torIcon.setValue(icon);
				return view;
			}
		};
		transports.add(createTransport(TorConstants.ID,
				R.drawable.transport_tor, R.string.transport_tor));
		transports.add(createTransport(LanTcpConstants.ID,
				R.drawable.transport_lan, R.string.transport_lan));
		transports.add(createTransport(BluetoothConstants.ID,
				R.drawable.transport_bt, R.string.transport_bt));
	}
	@ColorRes
	private int getIconColor(State state) {
		if (state == ACTIVE) return R.color.briar_lime_400;
		else if (state == ENABLING) return R.color.briar_orange_500;
		else return android.R.color.tertiary_text_light;
	}
	private Transport createTransport(TransportId id,
			@DrawableRes int iconDrawable, @StringRes int label) {
		int iconColor = getIconColor(STARTING_STOPPING);
		Transport transport = new Transport(id, iconDrawable, label, iconColor);
		pluginViewModel.getPluginState(id).observe(this, state -> {
			transport.iconColor = getIconColor(state);
			transportsAdapter.notifyDataSetChanged();
		});
		return transport;
	}
	private void showTransportsOnboarding(ImageView imageView) {
		int color = resolveColorAttribute(this, R.attr.colorControlNormal);
		Drawable drawable = VectorDrawableCompat
				.create(getResources(), R.drawable.transport_tor, null);
		new MaterialTapTargetPrompt.Builder(NavDrawerActivity.this,
				R.style.OnboardingDialogTheme).setTarget(imageView)
				.setPrimaryText(R.string.network_settings_title)
				.setSecondaryText(R.string.transports_onboarding_text)
				.setIconDrawable(drawable)
				.setIconDrawableColourFilter(color)
				.setBackgroundColour(
						ContextCompat.getColor(this, R.color.briar_primary))
				.show();
		navDrawerViewModel.transportsOnboardingShown();
	}
	private static class Transport {
		private final TransportId id;
		@DrawableRes
		private final int iconDrawable;
		@StringRes
		private final int label;
		@ColorRes
		private int iconColor;
		private Transport(TransportId id, @DrawableRes int iconDrawable,
				@StringRes int label, @ColorRes int iconColor) {
			this.id = id;
			this.iconDrawable = iconDrawable;
			this.label = label;
			this.iconColor = iconColor;
		}
	}
}