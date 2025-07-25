package org.nodex.api.client;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
@NotNullByDefault
public interface MessageTree<T extends MessageTree.MessageNode> {
	void add(Collection<T> nodes);
	void add(T node);
	void clear();
	List<T> depthFirstOrder();
	boolean contains(MessageId m);
	@NotNullByDefault
	interface MessageNode {
		MessageId getId();
		@Nullable
		MessageId getParentId();
		void setLevel(int level);
		long getTimestamp();
	}
}