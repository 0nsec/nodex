package org.nodex.android.conversation.glide;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import org.nodex.android.NodexApplication;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.io.InputStream;
import javax.inject.Inject;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public final class NodexModelLoader
		implements ModelLoader<AttachmentHeader, InputStream> {
	@Inject
	NodexDataFetcherFactory dataFetcherFactory;
	NodexModelLoader(NodexApplication app) {
		app.getApplicationComponent().inject(this);
	}
	@Override
	public LoadData<InputStream> buildLoadData(AttachmentHeader model,
			int width, int height, Options options) {
		ObjectKey key = new ObjectKey(model.getMessageId());
		NodexDataFetcher dataFetcher =
				dataFetcherFactory.createBriarDataFetcher(model);
		return new LoadData<>(key, dataFetcher);
	}
	@Override
	public boolean handles(AttachmentHeader model) {
		return true;
	}
}