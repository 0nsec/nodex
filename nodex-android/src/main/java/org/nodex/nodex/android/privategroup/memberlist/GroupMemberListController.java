package org.nodex.android.privategroup.memberlist;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.controller.DbController;
import org.nodex.android.controller.handler.ResultExceptionHandler;
import java.util.Collection;
public interface GroupMemberListController extends DbController {
	void loadMembers(GroupId groupId,
			ResultExceptionHandler<Collection<MemberListItem>, DbException> handler);
}