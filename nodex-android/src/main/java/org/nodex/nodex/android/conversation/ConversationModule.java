package org.nodex.android.conversation;
import org.nodex.android.activity.ActivityScope;
import org.nodex.android.conversation.glide.NodexDataFetcherFactory;
import dagger.Module;
import dagger.Provides;
@Module
public class ConversationModule {
	@ActivityScope
	@Provides
	NodexDataFetcherFactory provideBriarDataFetcherFactory(
			NodexDataFetcherFactory dataFetcherFactory) {
		return dataFetcherFactory;
	}
}