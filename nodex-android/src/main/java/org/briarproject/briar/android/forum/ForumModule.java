package org.nodex.android.forum;
import org.nodex.android.viewmodel.ViewModelKey;
import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
@Module
public interface ForumModule {
	@Binds
	@IntoMap
	@ViewModelKey(ForumListViewModel.class)
	ViewModel bindForumListViewModel(ForumListViewModel forumListViewModel);
	@Binds
	@IntoMap
	@ViewModelKey(ForumViewModel.class)
	ViewModel bindForumViewModel(ForumViewModel forumViewModel);
}