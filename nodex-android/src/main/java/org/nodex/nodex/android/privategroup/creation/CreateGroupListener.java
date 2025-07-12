package org.nodex.android.privategroup.creation;
import org.nodex.android.fragment.BaseFragment.BaseFragmentListener;
interface CreateGroupListener extends BaseFragmentListener {
	void onGroupNameChosen(String name);
}