package org.nodex.android.conversation;
import org.nodex.android.activity.ActivityScope;
import org.nodex.android.conversation.glide.BriarDataFetcherFactory;
import dagger.Module;
import dagger.Provides;
@Module
public class ConversationModule {
	@ActivityScope
	@Provides
	BriarDataFetcherFactory provideBriarDataFetcherFactory(
			BriarDataFetcherFactory dataFetcherFactory) {
		return dataFetcherFactory;
	}
}