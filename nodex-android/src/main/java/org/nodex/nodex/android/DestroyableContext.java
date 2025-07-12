package org.nodex.android;
public interface DestroyableContext {
	void runOnUiThreadUnlessDestroyed(Runnable runnable);
}