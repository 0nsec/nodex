package org.nodex.android.sharing;
import android.app.Activity;
import org.nodex.core.api.contact.event.ContactRemovedEvent;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.ClientId;
import org.nodex.core.api.sync.event.GroupAddedEvent;
import org.nodex.core.api.sync.event.GroupRemovedEvent;
import org.nodex.android.controller.DbControllerImpl;
import org.nodex.android.controller.handler.ResultExceptionHandler;
import org.nodex.api.sharing.InvitationItem;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import androidx.annotation.CallSuper;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.util.LogUtils.logDuration;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.core.util.LogUtils.now;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class InvitationControllerImpl<I extends InvitationItem>
		extends DbControllerImpl
		implements InvitationController<I>, EventListener {
	protected static final Logger LOG =
			Logger.getLogger(InvitationControllerImpl.class.getName());
	private final EventBus eventBus;
	protected InvitationListener listener;
	public InvitationControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, EventBus eventBus) {
		super(dbExecutor, lifecycleManager);
		this.eventBus = eventBus;
	}
	@Override
	public void onActivityCreate(Activity activity) {
		listener = (InvitationListener) activity;
	}
	@Override
	public void onActivityStart() {
		eventBus.addListener(this);
	}
	@Override
	public void onActivityStop() {
		eventBus.removeListener(this);
	}
	@Override
	public void onActivityDestroy() {
	}
	@CallSuper
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactRemovedEvent) {
			LOG.info("Contact removed, reloading...");
			listener.loadInvitations(true);
		} else if (e instanceof GroupAddedEvent) {
			GroupAddedEvent g = (GroupAddedEvent) e;
			ClientId cId = g.getGroup().getClientId();
			if (cId.equals(getShareableClientId())) {
				LOG.info("Group added, reloading");
				listener.loadInvitations(false);
			}
		} else if (e instanceof GroupRemovedEvent) {
			GroupRemovedEvent g = (GroupRemovedEvent) e;
			ClientId cId = g.getGroup().getClientId();
			if (cId.equals(getShareableClientId())) {
				LOG.info("Group removed, reloading");
				listener.loadInvitations(false);
			}
		}
	}
	protected abstract ClientId getShareableClientId();
	@Override
	public void loadInvitations(boolean clear,
			ResultExceptionHandler<Collection<I>, DbException> handler) {
		runOnDbThread(() -> {
			try {
				long start = now();
				Collection<I> invitations = new ArrayList<>(getInvitations());
				logDuration(LOG, "Loading invitations", start);
				handler.onResult(invitations);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			}
		});
	}
	@DatabaseExecutor
	protected abstract Collection<I> getInvitations() throws DbException;
}