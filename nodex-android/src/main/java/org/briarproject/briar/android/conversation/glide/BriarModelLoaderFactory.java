package org.nodex.android.conversation.glide;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import org.nodex.android.BriarApplication;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.InputStream;
@NotNullByDefault
class BriarModelLoaderFactory
		implements ModelLoaderFactory<AttachmentHeader, InputStream> {
	private final BriarApplication app;
	BriarModelLoaderFactory(BriarApplication app) {
		this.app = app;
	}
	@Override
	public ModelLoader<AttachmentHeader, InputStream> build(
			MultiModelLoaderFactory multiFactory) {
		return new BriarModelLoader(app);
	}
	@Override
	public void teardown() {
	}
}