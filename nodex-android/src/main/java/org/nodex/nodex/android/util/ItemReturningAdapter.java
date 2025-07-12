package org.nodex.android.util;
public interface ItemReturningAdapter<I> {
	I getItemAt(int position);
	int getItemCount();
}