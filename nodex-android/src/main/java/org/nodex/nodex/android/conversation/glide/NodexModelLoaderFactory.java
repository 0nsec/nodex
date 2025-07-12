package org.nodex.android.conversation.glide;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import org.nodex.android.NodexApplication;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.InputStream;
@NotNullByDefault
class NodexModelLoaderFactory
		implements ModelLoaderFactory<AttachmentHeader, InputStream> {
	private final NodexApplication app;
	NodexModelLoaderFactory(NodexApplication app) {
		this.app = app;
	}
	@Override
	public ModelLoader<AttachmentHeader, InputStream> build(
			MultiModelLoaderFactory multiFactory) {
		return new NodexModelLoader(app);
	}
	@Override
	public void teardown() {
	}
}