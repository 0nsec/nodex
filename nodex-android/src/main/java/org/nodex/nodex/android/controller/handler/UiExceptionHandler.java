package org.nodex.android.controller.handler;
import org.nodex.android.DestroyableContext;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import androidx.annotation.UiThread;
@Immutable
@NotNullByDefault
public abstract class UiExceptionHandler<E extends Exception>
		implements ExceptionHandler<E> {
	protected final DestroyableContext listener;
	protected UiExceptionHandler(DestroyableContext listener) {
		this.listener = listener;
	}
	@Override
	public void onException(E exception) {
		listener.runOnUiThreadUnlessDestroyed(() -> onExceptionUi(exception));
	}
	@UiThread
	public abstract void onExceptionUi(E exception);
}