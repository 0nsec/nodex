package org.nodex.privategroup.invitation;
import org.nodex.core.api.sync.Group.Visibility;
interface State {
	int getValue();
	Visibility getVisibility();
	boolean isAwaitingResponse();
}