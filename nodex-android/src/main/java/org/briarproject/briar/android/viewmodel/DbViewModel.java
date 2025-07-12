package org.nodex.android.viewmodel;
import android.app.Application;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbCallable;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.DbRunnable;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.android.util.UiUtils;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;
import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logException;
@Immutable
@NotNullByDefault
public abstract class DbViewModel extends AndroidViewModel {
	private static final Logger LOG = getLogger(DbViewModel.class.getName());
	@DatabaseExecutor
	private final Executor dbExecutor;
	protected final LifecycleManager lifecycleManager;
	private final TransactionManager db;
	protected final AndroidExecutor androidExecutor;
	public DbViewModel(
			Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor) {
		super(application);
		this.dbExecutor = dbExecutor;
		this.lifecycleManager = lifecycleManager;
		this.db = db;
		this.androidExecutor = androidExecutor;
	}
	protected void runOnDbThread(Runnable task) {
		dbExecutor.execute(() -> {
			try {
				lifecycleManager.waitForDatabase();
				task.run();
			} catch (InterruptedException e) {
				LOG.warning("Interrupted while waiting for database");
				Thread.currentThread().interrupt();
			}
		});
	}
	protected void runOnDbThread(boolean readOnly,
			DbRunnable<Exception> task, Consumer<Exception> err) {
		dbExecutor.execute(() -> {
			try {
				lifecycleManager.waitForDatabase();
				db.transaction(readOnly, task);
			} catch (InterruptedException e) {
				LOG.warning("Interrupted while waiting for database");
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				err.accept(e);
			}
		});
	}
	protected <T> void loadFromDb(DbCallable<T, DbException> task,
			UiConsumer<LiveResult<T>> uiConsumer) {
		dbExecutor.execute(() -> {
			try {
				lifecycleManager.waitForDatabase();
				db.transaction(true, txn -> {
					T t = task.call(txn);
					txn.attach(() -> uiConsumer.accept(new LiveResult<>(t)));
				});
			} catch (InterruptedException e) {
				LOG.warning("Interrupted while waiting for database");
				Thread.currentThread().interrupt();
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				androidExecutor.runOnUiThread(
						() -> uiConsumer.accept(new LiveResult<>(e)));
			}
		});
	}
	@NotNullByDefault
	public interface UiConsumer<T> {
		@UiThread
		void accept(T t);
	}
	@Nullable
	protected <T> List<T> addListItem(@Nullable List<T> list, T item) {
		if (list == null) return null;
		List<T> copy = new ArrayList<>(list);
		copy.add(item);
		return copy;
	}
	@Nullable
	protected <T> List<T> addListItems(@Nullable List<T> list,
			Collection<T> items) {
		if (list == null) return null;
		List<T> copy = new ArrayList<>(list);
		copy.addAll(items);
		return copy;
	}
	@Nullable
	protected <T> List<T> updateListItems(@Nullable List<T> list,
			Function<T, Boolean> test, Function<T, T> replacer) {
		if (list == null) return null;
		List<T> copy = new ArrayList<>(list);
		ListIterator<T> iterator = copy.listIterator();
		boolean changed = false;
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (test.apply(item)) {
				changed = true;
				iterator.set(replacer.apply(item));
			}
		}
		return changed ? copy : null;
	}
	@Nullable
	protected <T> List<T> removeListItems(@Nullable List<T> list,
			Function<T, Boolean> test) {
		if (list == null) return null;
		List<T> copy = new ArrayList<>(list);
		ListIterator<T> iterator = copy.listIterator();
		boolean changed = false;
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (test.apply(item)) {
				changed = true;
				iterator.remove();
			}
		}
		return changed ? copy : null;
	}
	@UiThread
	protected <T> void removeAndUpdateListItems(
			MutableLiveData<LiveResult<List<T>>> liveData,
			Function<T, Boolean> test) {
		List<T> copy = removeListItems(getList(liveData), test);
		if (copy != null) liveData.setValue(new LiveResult<>(copy));
	}
	@Nullable
	protected <T> List<T> getList(LiveData<LiveResult<List<T>>> liveData) {
		LiveResult<List<T>> value = liveData.getValue();
		if (value == null) return null;
		return value.getResultOrNull();
	}
	@AnyThread
	protected void handleException(Exception e) {
		UiUtils.handleException(getApplication(), androidExecutor, LOG, e);
	}
}