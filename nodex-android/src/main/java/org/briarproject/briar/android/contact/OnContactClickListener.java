package org.nodex.android.contact;
import android.view.View;
public interface OnContactClickListener<I> {
	void onItemClick(View view, I item);
}