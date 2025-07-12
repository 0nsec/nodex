package org.nodex.android.forum;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;
import org.nodex.core.api.db.DbException;
import org.nodex.core.util.StringUtils;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.BriarActivity;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumManager;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.util.LogUtils.logDuration;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.core.util.LogUtils.now;
import static org.nodex.android.util.UiUtils.enterPressed;
import static org.nodex.android.util.UiUtils.hideSoftKeyboard;
import static org.nodex.api.forum.ForumConstants.MAX_FORUM_NAME_LENGTH;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class CreateForumActivity extends BriarActivity {
	private static final Logger LOG =
			Logger.getLogger(CreateForumActivity.class.getName());
	private TextInputLayout nameEntryLayout;
	private EditText nameEntry;
	private Button createForumButton;
	private ProgressBar progress;
	@Inject
	protected volatile ForumManager forumManager;
	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_create_forum);
		nameEntryLayout = findViewById(R.id.createForumNameLayout);
		nameEntry = findViewById(R.id.createForumNameEntry);
		nameEntry.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start,
					int lengthBefore, int lengthAfter) {
				enableOrDisableCreateButton();
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		nameEntry.setOnEditorActionListener((v, actionId, e) -> {
			if (actionId == IME_ACTION_DONE || enterPressed(actionId, e)) {
				createForum();
				return true;
			}
			return false;
		});
		createForumButton = findViewById(R.id.createForumButton);
		createForumButton.setOnClickListener(v -> createForum());
		progress = findViewById(R.id.createForumProgressBar);
	}
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
	private void enableOrDisableCreateButton() {
		if (createForumButton == null) return;
		createForumButton.setEnabled(validateName());
	}
	private boolean validateName() {
		String name = nameEntry.getText().toString();
		int length = StringUtils.toUtf8(name).length;
		if (length > MAX_FORUM_NAME_LENGTH) {
			nameEntryLayout.setError(getString(R.string.name_too_long));
			return false;
		}
		nameEntryLayout.setError(null);
		return length > 0;
	}
	private void createForum() {
		if (!validateName()) return;
		hideSoftKeyboard(nameEntry);
		createForumButton.setVisibility(GONE);
		progress.setVisibility(VISIBLE);
		storeForum(nameEntry.getText().toString());
	}
	private void storeForum(String name) {
		runOnDbThread(() -> {
			try {
				long start = now();
				Forum f = forumManager.addForum(name);
				logDuration(LOG, "Storing forum", start);
				displayForum(f);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				finishOnUiThread();
			}
		});
	}
	private void displayForum(Forum f) {
		runOnUiThreadUnlessDestroyed(() -> {
			Intent i = new Intent(CreateForumActivity.this,
					ForumActivity.class);
			i.putExtra(GROUP_ID, f.getId().getBytes());
			i.putExtra(GROUP_NAME, f.getName());
			startActivity(i);
			Toast.makeText(CreateForumActivity.this,
					R.string.forum_created_toast, LENGTH_LONG).show();
			supportFinishAfterTransition();
		});
	}
}