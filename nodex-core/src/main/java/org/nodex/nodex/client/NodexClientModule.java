package org.nodex.client;
import org.nodex.api.client.MessageTracker;
import dagger.Module;
import dagger.Provides;
@Module
public class NodexClientModule {
	@Provides
	MessageTracker provideMessageTracker(MessageTrackerImpl messageTracker) {
		return messageTracker;
	}
}