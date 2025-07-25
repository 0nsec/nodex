package org.nodex.android.fragment;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import javax.inject.Inject;
public abstract class BaseEventFragment extends BaseFragment implements
		EventListener {
	@Inject
	protected volatile EventBus eventBus;
	@Override
	public void onStart() {
		super.onStart();
		eventBus.addListener(this);
	}
	@Override
	public void onStop() {
		super.onStop();
		eventBus.removeListener(this);
	}
}