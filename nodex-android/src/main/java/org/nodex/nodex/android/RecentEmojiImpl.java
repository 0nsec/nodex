package org.nodex.android;
import com.vanniktech.emoji.EmojiRange;
import com.vanniktech.emoji.EmojiUtils;
import com.vanniktech.emoji.RecentEmoji;
import com.vanniktech.emoji.emoji.Emoji;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import org.nodex.core.api.settings.Settings;
import org.nodex.core.api.settings.SettingsManager;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.core.util.StringUtils;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class RecentEmojiImpl implements RecentEmoji, OpenDatabaseHook {
	private static final Logger LOG =
			Logger.getLogger(RecentEmojiImpl.class.getName());
	private static final String EMOJI_LRU_PREFERENCE = "pref_emoji_recent2";
	private static final int EMOJI_LRU_SIZE = 50;
	private final LinkedList<Emoji> recentlyUsed = new LinkedList<>();
	private final Executor dbExecutor;
	private final AndroidExecutor androidExecutor;
	private final SettingsManager settingsManager;
	@Inject
	RecentEmojiImpl(@DatabaseExecutor Executor dbExecutor,
			AndroidExecutor androidExecutor, SettingsManager settingsManager) {
		this.dbExecutor = dbExecutor;
		this.androidExecutor = androidExecutor;
		this.settingsManager = settingsManager;
	}
	@Override
	public Collection<Emoji> getRecentEmojis() {
		return new ArrayList<>(recentlyUsed);
	}
	@Override
	public void addEmoji(Emoji emoji) {
		recentlyUsed.remove(emoji);
		recentlyUsed.add(0, emoji);
		if (recentlyUsed.size() > EMOJI_LRU_SIZE) recentlyUsed.removeLast();
	}
	@Override
	public void persist() {
		if (!recentlyUsed.isEmpty()) save(serialize(recentlyUsed));
	}
	@Override
	public void onDatabaseOpened(Transaction txn) throws DbException {
		Settings settings =
				settingsManager.getSettings(txn, SETTINGS_NAMESPACE);
		String serialized = settings.get(EMOJI_LRU_PREFERENCE);
		if (serialized != null) {
			androidExecutor.runOnUiThread(() ->
					recentlyUsed.addAll(deserialize(serialized)));
		}
	}
	private String serialize(Collection<Emoji> emojis) {
		Collection<String> strings = new ArrayList<>(emojis.size());
		for (Emoji emoji : emojis) strings.add(emoji.getUnicode());
		return StringUtils.join(strings, "\t");
	}
	private Collection<Emoji> deserialize(String serialized) {
		Collection<EmojiRange> ranges = EmojiUtils.emojis(serialized);
		Collection<Emoji> result = new ArrayList<>(ranges.size());
		for (EmojiRange range : ranges) result.add(range.emoji);
		return result;
	}
	private void save(String serialized) {
		dbExecutor.execute(() -> {
			Settings settings = new Settings();
			settings.put(EMOJI_LRU_PREFERENCE, serialized);
			try {
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}
}