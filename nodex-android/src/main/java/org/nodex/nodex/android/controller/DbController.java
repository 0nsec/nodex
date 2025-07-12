package org.nodex.android.controller;
import org.nodex.nullsafety.NotNullByDefault;
@Deprecated
@NotNullByDefault
public interface DbController {
	void runOnDbThread(Runnable task);
}