package org.nodex.android.removabledrive;
import org.nodex.core.api.plugin.file.RemovableDriveTask;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
abstract class TransferDataState {
	static class NoDataToSend extends TransferDataState {
	}
	static class NotSupported extends TransferDataState {
	}
	static class Ready extends TransferDataState {
	}
	static class TaskAvailable extends TransferDataState {
		final RemovableDriveTask.State state;
		TaskAvailable(RemovableDriveTask.State state) {
			this.state = state;
		}
	}
}