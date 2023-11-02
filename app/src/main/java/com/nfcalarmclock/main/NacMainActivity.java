package com.nfcalarmclock.main;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.nfcalarmclock.BuildConfig;
import com.nfcalarmclock.R;
import com.nfcalarmclock.activealarm.NacActiveAlarmService;
import com.nfcalarmclock.alarm.NacAlarmViewModel;
import com.nfcalarmclock.alarm.db.NacAlarm;
import com.nfcalarmclock.audiooptions.NacAlarmAudioOptionsDialog;
import com.nfcalarmclock.audiosource.NacAudioSourceDialog;
import com.nfcalarmclock.card.NacCardAdapter;
import com.nfcalarmclock.card.NacCardAdapterLiveData;
import com.nfcalarmclock.card.NacCardHolder;
import com.nfcalarmclock.card.NacCardTouchHelper;
import com.nfcalarmclock.dismissearly.NacDismissEarlyDialog;
import com.nfcalarmclock.graduallyincreasevolume.NacGraduallyIncreaseVolumeDialog;
import com.nfcalarmclock.mediapicker.NacMediaActivity;
import com.nfcalarmclock.nfc.NacNfc;
import com.nfcalarmclock.nfc.NacNfcTag;
import com.nfcalarmclock.nfc.NacScanNfcTagDialog;
import com.nfcalarmclock.permission.NacPermissionRequestManager;
import com.nfcalarmclock.ratemyapp.NacRateMyApp;
import com.nfcalarmclock.restrictvolume.NacRestrictVolumeDialog;
import com.nfcalarmclock.settings.NacMainSettingActivity;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.shutdown.NacShutdownBroadcastReceiver;
import com.nfcalarmclock.statistics.NacAlarmStatisticRepository;
import com.nfcalarmclock.tts.NacTextToSpeechDialog;
import com.nfcalarmclock.upcomingalarm.NacUpcomingAlarmNotification;
import com.nfcalarmclock.util.NacCalendar;
import com.nfcalarmclock.util.NacContext;
import com.nfcalarmclock.util.NacIntent;
import com.nfcalarmclock.util.NacUtility;
import com.nfcalarmclock.view.snackbar.NacSnackbar;
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The application's main activity.
 */
public class NacMainActivity
	extends AppCompatActivity
	implements Observer<List<NacAlarm>>,
		View.OnCreateContextMenuListener,
		Toolbar.OnMenuItemClickListener,
		RecyclerView.OnItemTouchListener,
		NacCardTouchHelper.OnSwipedListener,
		NacCardAdapter.OnViewHolderBoundListener,
		NacCardAdapter.OnViewHolderCreatedListener,
		NacCardHolder.OnCardCollapsedListener,
		NacCardHolder.OnCardDeleteClickedListener,
		NacCardHolder.OnCardExpandedListener,
		NacCardHolder.OnCardMediaClickedListener,
		NacCardHolder.OnCardAudioOptionsClickedListener,
		NacCardHolder.OnCardUpdatedListener,
		NacCardHolder.OnCardUseNfcChangedListener,
        NacScanNfcTagDialog.OnScanNfcTagListener,
        NacAlarmAudioOptionsDialog.OnAudioOptionClickedListener,
        NacAudioSourceDialog.OnAudioSourceSelectedListener,
		NacDismissEarlyDialog.OnDismissEarlyOptionSelectedListener,
		NacGraduallyIncreaseVolumeDialog.OnGraduallyIncreaseVolumeListener,
		NacRestrictVolumeDialog.OnRestrictVolumeListener,
		NacTextToSpeechDialog.OnTextToSpeechOptionsSelectedListener,
		NacWhatsNewDialog.OnReadWhatsNewListener
{

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Top toolbar.
	 */
	private MaterialToolbar mToolbar;

	/**
	 * Next alarm text view.
	 */
	private MaterialTextView mNextAlarmTextView;

	/**
	 * Recycler view containing the alarm cards.
	 */
	private RecyclerView mRecyclerView;

	/**
	 * Floating action button to add new alarms.
	 */
	private FloatingActionButton mFloatingActionButton;

	/**
	 * Alarm card adapter.
	 */
	private NacCardAdapter mAlarmCardAdapter;

	/**
	 * Scan an NFC tag dialog.
	 */
	private NacScanNfcTagDialog mScanNfcTagDialog;

	/**
	 * Shutdown broadcast receiver.
	 * <p>
	 * TODO: Should this be final like mTimeTickReceiver?
	 */
	private NacShutdownBroadcastReceiver mShutdownBroadcastReceiver;

	/**
	 * Alarm statistic repository.
	 */
	private NacAlarmStatisticRepository mAlarmStatisticRepository;

	/**
	 * Alarm view model.
	 */
	private NacAlarmViewModel mAlarmViewModel;

	/**
	 * Mutable live data for the alarm card that can be modified and sorted, or
	 * not sorted, depending on the circumstance.
	 * <p>
	 * Live data from the view model cannot be sorted, hence the need for this.
	 */
	private NacCardAdapterLiveData mAlarmCardAdapterLiveData;

	/**
	 * Alarm card touch helper.
	 */
	private NacCardTouchHelper mAlarmCardTouchHelper;

	/**
	 * The IDs of alarms that were recently added.
	 */
	private List<Long> mRecentlyAddedAlarmIds;

	/**
	 * The IDs of alarms that were recently updated.
	 */
	private List<Long> mRecentlyUpdatedAlarmIds;

	/**
	 * Alarm that is being used by an open audio options dialog.
	 */
	private NacAlarm mAudioOptionsAlarm;

	/**
	 * Last action on an alarm card.
	 */
	private NacLastAlarmCardAction mLastAlarmCardAction;

	/**
	 * The snackbar.
	 */
	private NacSnackbar mSnackbar;

	/**
	 * View of the last card clicked.
	 */
	private View mLastCardClicked;

	/**
	 * Indicator of whether the activity is shown or not.
	 */
	private boolean mIsActivityShown = false;

	/**
	 * The NFC tag that was scanned for an active alarm.
	 */
	private NacNfcTag mNfcTag = null;

	/**
	 * Permission request manager, handles requesting permissions from the user.
	 */
	private NacPermissionRequestManager mPermissionRequestManager;

	/**
	 * Listener for when the floating action button is clicked.
	 */
	private final View.OnClickListener mFloatingActionButtonListener =
			view -> {
				NacSharedPreferences shared = getSharedPreferences();
				NacCardAdapter adapter = getAlarmCardAdapter();
				int size = adapter.getItemCount();
				int maxAlarms = getResources().getInteger(R.integer.max_alarms);

				// Haptic feedback so that the user knows the action was received
				view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				// Max number of alarms reached
				if ((size+1) > maxAlarms)
				{
					String message = getString(R.string.error_message_max_alarms);

					NacUtility.quickToast(NacMainActivity.this, message);
					return;
				}

				// Create and add the alarm
				NacAlarm alarm = new NacAlarm.Builder(shared).build();
				addAlarm(alarm);
			};


	/**
	 * Capture the click event on the Snackbar button.
	 */
	private final View.OnClickListener mOnSwipeSnackbarActionListener =
			view -> {
				NacLastAlarmCardAction lastAction = getLastAlarmCardAction();
				NacAlarm alarm = lastAction.getAlarm();

				// Delete alarm (undo copy)
				if (lastAction.wasCopy())
				{
					deleteAlarm(alarm);
				}
				// Restore alarm (undo delete)
				else if (lastAction.wasDelete())
				{
					restoreAlarm(alarm);
				}
				// Delete alarm (undo restore)
				else if (lastAction.wasRestore())
				{
					deleteAlarm(alarm);
				}
            };

	/**
	 * Receiver for the time tick intent. This is called when the time increments
	 * every minute.
	 * <p>
	 * TODO: Should this be its own class like NacShutdownBroadcastReceiver?
	 */
	private final BroadcastReceiver mTimeTickReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			setNextAlarmMessage();
			refreshAlarmsThatWillAlarmSoon();
		}
	};

	/**
	 * Add an alarm to the database.
	 *
	 * @param  alarm  An alarm.
	 */
	private void addAlarm(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		// Insert alarm
		long id = this.getAlarmViewModel().insert(this, alarm);

		// Set the ID of the alarm
		if (alarm.getId() <= 0)
		{
			alarm.setId(id);
		}

		// Save the recently added alarm ID
		this.getRecentlyAddedAlarmIds().add(id);

		// Save the statistics
		this.getAlarmStatisticRepository().insertCreated();
	}

	/**
	 * Add the first alarm, when the app is first run.
	 */
	private void addFirstAlarm()
	{
		// Get the shared prefereneces
		NacSharedPreferences shared = this.getSharedPreferences();

		// Create the alarm
		NacAlarm alarm = new NacAlarm.Builder(shared)
			.setId(0)
			.setHour(8)
			.setMinute(0)
			.setName("Work")
			.build();

		// Add the alarm
		this.addAlarm(alarm);

		// Avoid having interact() called for the alarm card, that way it does not
		// get expanded and show the time dialog
		long id = alarm.getId();
		this.getRecentlyAddedAlarmIds().remove(id);
	}

	/**
	 * Add an alarm that was created from the SET_ALARM intent.
	 */
	private void addSetAlarmFromIntent()
	{
		Intent intent = getIntent();
		NacAlarm alarm = NacIntent.getSetAlarm(this, intent);

		this.addAlarm(alarm);
	}

	/**
	 * Cleanup the show activity delay handler.
	 */
	private void cleanupScanNfcTagDialog()
	{
		NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();

		// Check that the dialog is not null
		if (dialog != null)
		{
			// Dismiss the dialog
			dialog.dismissAllowingStateLoss();

			// Cleanup the dialog
			this.mScanNfcTagDialog = null;
		}
	}

	/**
	 * Cleanup the shutdown broadcast receiver.
	 */
	private void cleanupShutdownBroadcastReceiver()
	{
		NacShutdownBroadcastReceiver receiver = this.getShutdownBroadcastReceiver();

		if (receiver != null)
		{
			try
			{
				// Unregister the receiver
				unregisterReceiver(receiver);
			}
			catch (IllegalArgumentException e)
			{
				//e.printStackTrace();
			}
		}
	}

	/**
	 * Cleanup the time tick receiver.
	 */
	private void cleanupTimeTickReceiver()
	{
		BroadcastReceiver receiver = this.getTimeTickReceiver();

		try
		{
			// Unregister the receiver
			unregisterReceiver(receiver);
		}
		catch (IllegalArgumentException e)
		{
			//e.printStackTrace();
		}

	}

	/**
	 * TODO: Catch exceptions properly
	 */
	public void copyAlarm(NacAlarm alarm)
	{
		String message = getString(R.string.message_alarm_copy);
		String action = getString(R.string.action_undo);

		NacAlarm copiedAlarm = alarm.copy();

		this.addAlarm(copiedAlarm);
		this.getLastAlarmCardAction().set(copiedAlarm,
			NacLastAlarmCardAction.Type.COPY);
		this.showSnackbar(message, action, this.mOnSwipeSnackbarActionListener);
	}

	/**
	 * Delete an alarm from the database.
	 *
	 * @param  alarm  An alarm.
	 */
	public void deleteAlarm(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		String message = getString(R.string.message_alarm_delete);
		String action = getString(R.string.action_undo);

		this.getAlarmViewModel().delete(this, alarm);
		this.getAlarmStatisticRepository().insertDeleted(alarm);
		this.getLastAlarmCardAction().set(alarm, NacLastAlarmCardAction.Type.DELETE);
		this.showSnackbar(message, action, this.mOnSwipeSnackbarActionListener);
	}

	/**
	 * Disable the alias for the main activity so that tapping an NFC tag
	 * DOES NOT open the main activity.
	 */
	private void disableActivityAlias()
	{
		PackageManager packageManager = getPackageManager();
		String packageName = getPackageName();
		String aliasName = packageName + ".main.NacMainAliasActivity";
		ComponentName componentName = new ComponentName(this, aliasName);

		packageManager.setComponentEnabledSetting(componentName,
			PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
			PackageManager.DONT_KILL_APP);
	}

	/**
	 * Attempt to dismiss the first active alarm found.
	 * <p>
	 * If unable to dismiss the alarm, the alarm activity is shown.
	 */
	private void dismissActiveAlarm()
	{
		NacNfcTag tag = this.getNfcTag();

		if (tag == null)
		{
			return;
		}
		else if (tag.check(this))
		{
			NacContext.dismissAlarmActivityWithNfc(this, tag);
		}
		else
		{
			NacContext.startAlarm(this, tag.getActiveAlarm());
		}

		this.mNfcTag = null;
	}

	/**
	 * @return The alarm card adapter.
	 */
	private NacCardAdapter getAlarmCardAdapter()
	{
		return this.mAlarmCardAdapter;
	}

	/**
	 * @return Mutable live data that is used to submit data to the alarm card
	 *     adapter.
	 */
	private NacCardAdapterLiveData getAlarmCardAdapterLiveData()
	{
		return this.mAlarmCardAdapterLiveData;
	}

	/**
	 * @return The alarm card at the given index.
	 */
	private NacCardHolder getAlarmCardAt(int index)
	{
		RecyclerView rv = this.getRecyclerView();

		return (NacCardHolder) rv.findViewHolderForAdapterPosition(index);
	}

	/**
	 * @return The alarm card touch helper.
	 */
	private NacCardTouchHelper getAlarmCardTouchHelper()
	{
		return this.mAlarmCardTouchHelper;
	}

	/**
	 * @return The alarm statistic repository.
	 */
	private NacAlarmStatisticRepository getAlarmStatisticRepository()
	{
		return this.mAlarmStatisticRepository;
	}

	/**
	 * @return The alarm view model.
	 */
	private NacAlarmViewModel getAlarmViewModel()
	{
		return this.mAlarmViewModel;
	}

	/**
	 * @return The alarm being used by an open audio options dialog.
	 */
	private NacAlarm getAudioOptionsAlarm()
	{
		return this.mAudioOptionsAlarm;
	}

	/**
	 * @return The number of alarm cards that are expanded.
	 */
	private int getCardsExpandedCount()
	{
		RecyclerView rv = this.getRecyclerView();
		NacCardAdapter adapter = this.getAlarmCardAdapter();

		return adapter.getCardsExpandedCount(rv);
	}

	/**
	 * @return The floating action button.
	 */
	private FloatingActionButton getFloatingActionButton()
	{
		return this.mFloatingActionButton;
	}

	/**
	 * @return The last alarm card action.
	 */
	private NacLastAlarmCardAction getLastAlarmCardAction()
	{
		return this.mLastAlarmCardAction;
	}

	/**
	 * Get the message to show for the next alarm.
	 *
	 * @param  alarms  List of alarms.
	 *
	 * @return The message to show for the next alarm.
	 */
	public String getNextAlarmMessage(List<NacAlarm> alarms)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm nextAlarm = NacCalendar.getNextAlarm(alarms);

		return NacCalendar.getMessageNextAlarm(shared, nextAlarm);
	}

	/**
	 * Get the next alarm text view.
	 *
	 * @return The next alarm text view.
	 */
	private MaterialTextView getNextAlarmTextView()
	{
		return this.mNextAlarmTextView;
	}

	/**
	 * Get the NFC tag.
	 *
	 * @return The NFC tag.
	 */
	private NacNfcTag getNfcTag()
	{
		return this.mNfcTag;
	}

	/**
	 * Get the permission request manager.
	 *
	 * @return The permission request manager.
	 */
	private NacPermissionRequestManager getPermissionRequestManager()
	{
		return this.mPermissionRequestManager;
	}

	/**
	 * @return List of IDs for alarms that were recently added.
	 */
	private List<Long> getRecentlyAddedAlarmIds()
	{
		return this.mRecentlyAddedAlarmIds;
	}

	/**
	 * @return List of IDs for alarms that were recently updated.
	 */
	private List<Long> getRecentlyUpdatedAlarmIds()
	{
		return this.mRecentlyUpdatedAlarmIds;
	}

	/**
	 * @return The recycler view.
	 */
	private RecyclerView getRecyclerView()
	{
		return this.mRecyclerView;
	}

	/**
	 * @return Scan NFC tag dialog.
	 */
	private NacScanNfcTagDialog getScanNfcTagDialog()
	{
		return this.mScanNfcTagDialog;
	}

	/**
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 * Get the shutdown broadcast receiver.
	 */
	private NacShutdownBroadcastReceiver getShutdownBroadcastReceiver()
	{
		return this.mShutdownBroadcastReceiver;
	}

	/**
	 * @return The snackbar.
	 */
	private NacSnackbar getSnackbar()
	{
		return this.mSnackbar;
	}

	/**
	 * Get the time tick receiver.
	 *
	 * @return The time tick receiver
	 */
	private BroadcastReceiver getTimeTickReceiver()
	{
		return this.mTimeTickReceiver;
	}

	/**
	 * Get the toolbar.
	 *
	 * @return The toolbar.
	 */
	private MaterialToolbar getToolbar()
	{
		return this.mToolbar;
	}

	/**
	 * @return True if the activity is shown, and False otherwise.
	 */
	private boolean isActivityShown()
	{
		return this.mIsActivityShown;
	}

	/**
	 * @return True if an NFC tag was scanned to dismiss an alarm and is ready
	 *     for the active alarm activity, and False otherwise.
	 */
	private boolean isNfcTagReady()
	{
		NacNfcTag tag = this.getNfcTag();

		return (tag != null) && tag.isReady();
	}

	/**
	 * Called when an item in the audio options dialog is clicked.
	 */
	@Override
	public void onAudioOptionClicked(long alarmId, int which)
	{
		this.mAudioOptionsAlarm = this.getAlarmViewModel().findAlarm(alarmId);

		switch (which)
		{
			case 0:
				this.showAudioSourceDialog();
				break;
			case 1:
				this.showDismissEarlyDialog();
				break;
			case 2:
				this.showGraduallyIncreaseVolumeDialog();
				break;
			case 3:
				this.showRestrictVolumeDialog();
				break;
			case 4:
				this.showTextToSpeechDialog();
				break;
			default:
				break;
		}
	}

	/**
	 * Called when an audio source is selected.
	 */
	@Override
	public void onAudioSourceSelected(String audioSource)
	{
		NacAlarm alarm = this.getAudioOptionsAlarm();

		alarm.setAudioSource(audioSource);
		this.getAlarmViewModel().update(this, alarm);
	}

	/**
	 * Called when the user cancels the scan NFC tag dialog.
	 */
	@Override
	public void onCancelNfcTagScan(NacAlarm alarm)
	{
		RecyclerView rv = this.getRecyclerView();

		// Check that the alarm is not null
		if (alarm != null)
		{

			// Get the card that corresponds to the alarm
			long id = alarm.getId();
			NacCardHolder cardHolder = (NacCardHolder) rv.findViewHolderForItemId(id);

			// Check that the card is not null
			if (cardHolder != null)
			{
				// Uncheck the NFC button when the dialog is canceled.
				cardHolder.getNfcButton().setChecked(false);
				cardHolder.doNfcButtonClick();
			}
		}

		// Cleanup the dialog
		this.cleanupScanNfcTagDialog();
	}

	/**
	 * Called when the audio options button is clicked in an alarm card.
	 */
	@Override
	public void onCardAudioOptionsClicked(NacCardHolder holder, NacAlarm alarm)
	{
		this.showAudioOptionsDialog(alarm);
	}

	/**
	 * Called when the alarm card is collapsed.
	 */
	@Override
	public void onCardCollapsed(NacCardHolder holder, NacAlarm alarm)
	{
		List<Long> updatedAlarms = this.getRecentlyUpdatedAlarmIds();
		long id = alarm.getId();

		// Sort the list when no cards are expanded
		if (this.getCardsExpandedCount() == 0)
		{
			this.getAlarmCardAdapterLiveData().sort();
		}

		// Show the next time the alarm will go off, as well as highlight the card
		// that was just collapsed
		if (updatedAlarms.contains(id))
		{
			this.showUpdatedAlarmSnackbar(alarm);
			holder.highlight();
			updatedAlarms.remove(id);
		}
	}

	/**
	 * Called when the delete button is clicked in an alarm card.
	 */
	@Override
	public void onCardDeleteClicked(NacCardHolder holder, NacAlarm alarm)
	{
		this.deleteAlarm(alarm);
	}

	/**
	 * Called when the alarm card is expanded.
	 */
	@Override
	public void onCardExpanded(NacCardHolder holder, NacAlarm alarm)
	{
	}

	/**
	 * Called when the media button is clicked in an alarm card.
	 */
	@Override
	public void onCardMediaClicked(NacCardHolder holder, NacAlarm alarm)
	{
		Intent intent = NacIntent.toIntent(this, NacMediaActivity.class, alarm);

		startActivity(intent);
	}

	/**
	 * Called when the alarm has been changed.
	 *
	 * @param  alarm  The alarm that was changed.
	 */
	@Override
	public void onCardUpdated(NacCardHolder holder, NacAlarm alarm)
	{
		// Set the next alarm message
		this.setNextAlarmMessage();

		// Card is collapsed
		if (holder.isCollapsed())
		{
			this.showUpdatedAlarmSnackbar(alarm);
			holder.highlight();
		}
		// Card is expanded
		else
		{
			long id = alarm.getId();
			this.getRecentlyUpdatedAlarmIds().add(id);
		}

		// Update the view model
		this.getAlarmViewModel().update(this, alarm);
	}

	/**
	 */
	@Override
	public void onCardUseNfcChanged(NacCardHolder holder, NacAlarm alarm)
	{
		if (!alarm.getShouldUseNfc())
		{
			return;
		}

		// Get the fragment manager
		FragmentManager manager = getSupportFragmentManager();

		// Create the dialog
		NacScanNfcTagDialog dialog = new NacScanNfcTagDialog();
		this.mScanNfcTagDialog = dialog;

		// Setup the dialog
		dialog.setAlarm(alarm);
		dialog.setOnScanNfcTagListener(this);

		// Show the dialog
		dialog.show(manager, NacScanNfcTagDialog.TAG);
	}

	/**
	 * Alarm list has changed.
	 * <p>
	 * TODO: There is a race condition between snoozing an alarm, writing to the
	 *       database, and refreshing the main activity.
	 */
	@Override
	public void onChanged(List<NacAlarm> alarms)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		RecyclerView rv = this.getRecyclerView();

		// If this is the first time the app is running, set the flags accordingly
		if (shared.getAppFirstRun(this))
		{
			this.setupForAppFirstRun();
		}

		// Update the notification if a user uses upcoming alarm notifications
		this.updateUpcomingNotification(alarms);

		// Update the alarm adapter
		this.getAlarmCardAdapter().storeIndicesOfExpandedCards(rv);
		this.getAlarmCardAdapter().submitList(alarms);

		// Check if the main activity should be refreshed and if so, refresh it
		// TODO: Why is this here?
		if (shared.getShouldRefreshMainActivity())
		{
			this.refreshMainActivity();
		}
	}

	/**
	 * Called when an alarm card was swiped to copy.
	 *
	 * @param  index  The index of the alarm card.
	 */
	@Override
	public void onCopySwipe(NacAlarm alarm, int index)
	{
		NacCardAdapter adapter = this.getAlarmCardAdapter();
		NacCardHolder card = this.getAlarmCardAt(index);
		int size = adapter.getItemCount();
		int maxAlarms = getResources().getInteger(R.integer.max_alarms);

		// Haptic feedback so that the user knows the action was received
		if (card != null)
		{
			card.getRoot().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		}

		// Reset the view on the alarm that was swiped
		adapter.notifyItemChanged(index);

		// Max number of alarms reached
		if ((size+1) > maxAlarms)
		{
			String message = getString(R.string.error_message_max_alarms);

			NacUtility.quickToast(this, message);
			return;
		}

		// Set the index of the new alarm that will be created. This way, the
		// the snackbar can undo any action on that alarm
		this.getLastAlarmCardAction().setIndex(size);

		// Copy the alarm
		this.copyAlarm(alarm);
	}

	/**
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);

		// Get the intent passed into the app
		Intent intent = getIntent();

		// Set member variables
		View root = findViewById(R.id.activity_main);
		NacSharedPreferences shared = new NacSharedPreferences(this);
		this.mToolbar = findViewById(R.id.tb_top_bar);
		this.mNextAlarmTextView = findViewById(R.id.tv_next_alarm);
		this.mFloatingActionButton = findViewById(R.id.fab_add_alarm);
		this.mRecyclerView = findViewById(R.id.rv_alarm_list);
		this.mSharedPreferences = shared;
		this.mAlarmStatisticRepository = new NacAlarmStatisticRepository(this);
		this.mAlarmViewModel = new ViewModelProvider(this).get(NacAlarmViewModel.class);
		this.mAlarmCardAdapterLiveData = new NacCardAdapterLiveData();
		this.mAlarmCardAdapter = new NacCardAdapter();
		this.mAlarmCardTouchHelper = new NacCardTouchHelper(this);
		this.mRecentlyAddedAlarmIds = new ArrayList<>();
		this.mRecentlyUpdatedAlarmIds = new ArrayList<>();
		this.mLastAlarmCardAction = new NacLastAlarmCardAction();
		this.mShutdownBroadcastReceiver = new NacShutdownBroadcastReceiver();
		this.mSnackbar = new NacSnackbar(root);
		this.mScanNfcTagDialog = null;
		this.mPermissionRequestManager = new NacPermissionRequestManager(this);

		// Setup
		this.getSharedPreferences().editCardIsMeasured(false);
		this.setupLiveDataObservers();
		this.setupToolbar();
		this.setupAlarmCardAdapter();
		this.setupRecyclerView();

		// NFC tag was scanned for an active alarm
		if (this.wasNfcScannedForActiveAlarm(intent))
		{
			this.setNfcTagIntent(intent);
		}

		// Disable the activity alias so that tapping an NFC tag will NOT open
		// the main activity
		this.disableActivityAlias();
	}

	/**
	 * Create the context menu.
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
		ContextMenuInfo menuInfo)
	{
		// Set the last card that was clicked
		this.mLastCardClicked = view;

		// Inflate the context menu
		getMenuInflater().inflate(R.menu.menu_card, menu);

		// Iterate over each menu item
		for (int i=0; i < menu.size(); i++)
		{
			MenuItem item = menu.getItem(i);

			// Set the listener for a menu item
			item.setOnMenuItemClickListener(menuItem ->
			{
				RecyclerView rv = getRecyclerView();
				NacCardHolder holder = (NacCardHolder) rv.findContainingViewHolder(mLastCardClicked);
				int id = menuItem.getItemId();

				// Check to make sure the card holder is not null
				if (holder != null)
				{
					NacAlarm alarm = holder.getAlarm();

					// Show the next time the alarm is scheduled to go off
					if (id == R.id.menu_show_next_alarm)
					{
						showAlarmSnackbar(alarm);
					}
					// Show the NFC tag ID
					else if (id == R.id.menu_show_nfc_tag_id)
					{
						showNfcTagId(alarm);
					}
				}

				// Reset the last clicked card to null
				mLastCardClicked = null;

				return true;
			});
		}
	}

	/**
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_action_bar, menu);
		return true;
	}

	/**
	 * Called when an alarm card was swiped to delete.
	 *
	 * @param  index  The index of the alarm card.
	 */
	@Override
	public void onDeleteSwipe(NacAlarm alarm, int index)
	{
		NacCardHolder card = this.getAlarmCardAt(index);

		// Haptic feedback so that the user knows the action was received
		if (card != null)
		{
			card.getRoot().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		}

		// Set the index of the new alarm that will be created. This way, the
		// the snackbar can undo any action on that alarm
		this.getLastAlarmCardAction().setIndex(index);

		// Delete the alarm
		this.deleteAlarm(alarm);
	}

	/**
	 * Called when the dismiss early alarm option is selected.
	 */
	@Override
	public void onDismissEarlyOptionSelected(boolean useDismissEarly, int index)
	{
		NacAlarm alarm = this.getAudioOptionsAlarm();

		alarm.setUseDismissEarly(useDismissEarly);
		alarm.setDismissEarlyTimeFromIndex(index);
		this.getAlarmViewModel().update(this, alarm);
	}

	/**
	 * Called when the alarm volume should/should not be gradually increased when
	 * an alarm goes off.
	 */
	@Override
	public void onGraduallyIncreaseVolume(boolean shouldIncrease)
	{
		NacAlarm alarm = this.getAudioOptionsAlarm();

		alarm.setShouldGraduallyIncreaseVolume(shouldIncrease);
		this.getAlarmViewModel().update(this, alarm);
	}

	/**
	 * Needed for RecyclerView.OnItemTouchListener
	 */
	public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, MotionEvent ev)
	{
		int action = ev.getAction();

		if (action == MotionEvent.ACTION_UP)
		{
			NacSnackbar snackbar = this.getSnackbar();

			if (snackbar.getCanDismiss())
			{
				snackbar.dismiss();
			}
		}

		return false;
	}

	/**
	 * Catch when a menu item is clicked.
	 */
	@Override
	public boolean onMenuItemClick(MenuItem item)
	{
		int id = item.getItemId();

		// Settings clicked
		if (id == R.id.menu_settings)
		{
			Intent settingsIntent = new Intent(this, NacMainSettingActivity.class);
			startActivity(settingsIntent);
			return true;
		}
		// Unknown
		else
		{
			return false;
		}
	}

	/**
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		// NFC tag was scanned for the NFC dialog
		if (this.wasNfcScannedForDialog(intent))
		{
			NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();
			String message = getString(R.string.message_nfc_required);

			// Save the NFC tag ID and show a toast
			this.saveNfcTagId(intent);
			NacUtility.quickToast(this, message);

			// Close the dialog
			if (dialog != null)
			{
				// Set the listener to null so that it does not get called
				dialog.setOnScanNfcTagListener(null);

				// Dismiss the dialog
				dialog.dismiss();
			}
		}
		// NFC tag was scanned for an active alarm
		else if (this.wasNfcScannedForActiveAlarm(intent))
		{
			this.setNfcTagIntent(intent);
			this.dismissActiveAlarm();
		}
	}

	/**
	 */
	@Override
	protected void onPause()
	{
		super.onPause();

		this.setIsActivityShown(false);
		this.cleanupTimeTickReceiver();
		this.cleanupShutdownBroadcastReceiver();
		NacNfc.stop(this);
	}

	/**
	 * Called when the What's New dialog has been read.
	 */
	@Override
	public void onReadWhatsNew()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		String version = BuildConfig.VERSION_NAME;

		// Set the previous app version as the current version. This way, the What's
		// New dialog does not show again
		shared.editPreviousAppVersion(version);
	}


	/**
	 * Note: Needed for RecyclerView.OnItemTouchListener
	 */
	public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept)
	{
	}

	/**
	 */
	@Override
	public void onRestrictVolume(boolean shouldRestrict)
	{
		NacAlarm alarm = this.getAudioOptionsAlarm();

		alarm.setShouldRestrictVolume(shouldRestrict);
		this.getAlarmViewModel().update(this, alarm);
	}

	/**
	 */
	@Override
	protected void onResume()
	{
		super.onResume();

		NacSharedPreferences shared = this.getSharedPreferences();

		// Check if the main activity should be refreshed and if so, refresh it
		if (shared.getShouldRefreshMainActivity())
		{
			this.refreshMainActivity();
			return;
		}

		// Set flag that the activity is being shown
		this.setIsActivityShown(true);

		// Set the next alarm text
		this.setNextAlarmMessage();

		// Setup UI
		this.setupFloatingActionButton();
		this.setupInitialDialogToShow();

		// Setup broadcast receivers
		this.setupTimeTickReceiver();
		this.setupShutdownBroadcastReceiver();

		// Setup NFC scanning detection
		NacNfc.start(this);

		// Add alarm from SET_ALARM intent (if it is present in intent)
		this.addSetAlarmFromIntent();
	}

	/**
	 * Called when a text-to-speech option is selected.
	 */
	@Override
	public void onTextToSpeechOptionsSelected(boolean useTts, int freq)
	{
		NacAlarm alarm = this.getAudioOptionsAlarm();

		alarm.setUseTts(useTts);
		alarm.setTtsFrequency(freq);
		this.getAlarmViewModel().update(this, alarm);
	}

	/**
	 * Note: Needed for RecyclerView.OnItemTouchListener
	 */
	public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e)
	{
	}

	/**
	 * Called when theh user wants to use any NFC tag.
	 */
	@Override
	public void onUseAnyNfcTag(NacAlarm alarm)
	{
		// Check that the alarm is null
		if (alarm == null)
		{
			return;
		}

		// Set the default (empty) NFC tag ID.
		alarm.setNfcTagId("");

		// Update the alarm
		this.getAlarmViewModel().update(this, alarm);

		// Cleanup the dialog
		this.cleanupScanNfcTagDialog();

		// Toast to the user
		String message = getString(R.string.message_nfc_required);

		NacUtility.quickToast(this, message);
	}

	/**
	 * Needed for NacAlarmCardAdapter.OnViewHolderBoundListener.
	 */
	@Override
	public void onViewHolderBound(NacCardHolder card, int index)
	{
		// Verify that the alarm card is measured. If it is not, it will be measured
		this.verifyCardIsMeasured(card);

		NacCardAdapter adapter = this.getAlarmCardAdapter();
		NacAlarm alarm = adapter.getAlarmAt(index);
		List<Long> addedAlarms = this.getRecentlyAddedAlarmIds();
		long id = alarm.getId();

		// Interact with recently added alarms, expanding them and showing the time
		// dialog
		if (addedAlarms.contains(id))
		{
			card.interact();
			addedAlarms.remove(id);
		}
	}

	/**
	 * Needed for NacAlarmCardAdapter.OnViewHolderCreatedListener.
	 */
	@Override
	public void onViewHolderCreated(final NacCardHolder card)
	{
		card.setOnCardCollapsedListener(this);
		card.setOnCardDeleteClickedListener(this);
		card.setOnCardExpandedListener(this);
		card.setOnCardMediaClickedListener(this);
		card.setOnCardAudioOptionsClickedListener(this);
		card.setOnCardUpdatedListener(this);
		card.setOnCardUseNfcChangedListener(this);
		card.setOnCreateContextMenuListener(this);
	}

	/**
	 * Prepare an active alarm to be shown to the user.
	 * <p>
	 * If the alarm is null, or the activity is not shown, it will not be run.
	 *
	 * @param  alarm  An active alarm.
	 */
	private void prepareActiveAlarm(NacAlarm alarm)
	{
		if (alarm != null)
		{
			this.setNfcTagAlarm(alarm);
		}

		if (this.isNfcTagReady())
		{
			this.dismissActiveAlarm();
		}
		else if (this.isActivityShown() && this.shouldShowAlarmActivity(alarm))
		{
			// TODO: This caused the active alarm to show up a million times!
			//NacSharedPreferences shared = this.getSharedPreferences();
			//Remove this setting: shared.getPreventAppFromClosing()?

			// Run the service only if it is not already running
			if (!NacActiveAlarmService.isRunning(this))
			{
				NacActiveAlarmService.startService(this, alarm);
			}

			// Start the alarm activity
			NacContext.startAlarmActivity(this, alarm);
		}
	}

	/**
	 * Refresh alarms that will alarm soon.
	 */
	private void refreshAlarmsThatWillAlarmSoon()
	{
		NacCardAdapter adapter = this.getAlarmCardAdapter();
		int length = adapter.getItemCount();

		// Iterate over each alarm card in the adapter
		for (int i=0; i < length; i++)
		{
			NacCardHolder card = this.getAlarmCardAt(i);
			NacAlarm a = adapter.getAlarmAt(i);

			// Alarm will alarm soon and the card needs to be updated
			if ((a != null) && (card != null) && a.willAlarmSoon()
				&& card.shouldRefreshDismissView())
			{
				// Refresh the alarm
				adapter.notifyItemChanged(i);
			}
		}
	}

	/**
	 * Refresh the main activity.
	 */
	private void refreshMainActivity()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		shared.editShouldRefreshMainActivity(false);
		recreate();
	}

	/**
	 * Restore an alarm and add it back to the database.
	 *
	 * @param  alarm  An alarm.
	 */
	public void restoreAlarm(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		String message = getString(R.string.message_alarm_restore);
		String action = getString(R.string.action_undo);

		this.getLastAlarmCardAction().set(alarm, NacLastAlarmCardAction.Type.RESTORE);
		this.getAlarmViewModel().insert(this, alarm);
		this.showSnackbar(message, action, this.mOnSwipeSnackbarActionListener);
	}

	/**
	 * Save the scanned NFC tag ID.
	 */
	private void saveNfcTagId(Intent intent)
	{
		// Check if the intent corresponds with the scan NFC tag dialog
		if (!this.wasNfcScannedForDialog(intent))
		{
			return;
		}

		Tag nfcTag = NacNfc.getTag(intent);

		// Check that the intent tag is null
		if (nfcTag == null)
		{
			return;
		}

		// Get the dialog
		NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();

		// Get the alarm and NFC tag ID
		NacAlarm alarm = dialog.getAlarm();
		String id = NacNfc.parseId(nfcTag);

		// Check to make sure the alarm is not null before proceeding
		if (alarm == null)
		{
			return;
		}

		// Set the NFC tag ID
		alarm.setNfcTagId(id);

		// Update the alarm
		this.getAlarmViewModel().update(this, alarm);
	}

	/**
	 * Set whether the activity is shown or not.
	 * 
	 * @param  isShown  Whether the activity is shown or not.
	 */
	private void setIsActivityShown(boolean isShown)
	{
		this.mIsActivityShown = isShown;
	}

	/**
	 * Set the next alarm message in the text view.
	 */
	private void setNextAlarmMessage()
	{
		NacCardAdapter cardAdapter = this.getAlarmCardAdapter();
		List<NacAlarm> alarms = cardAdapter.getCurrentList();

		this.setNextAlarmMessage(alarms);
	}

	/**
	 * Set the next alarm message in the text view.
	 */
	private void setNextAlarmMessage(List<NacAlarm> alarms)
	{
		MaterialTextView nextAlarm = this.getNextAlarmTextView();
		String message = this.getNextAlarmMessage(alarms);

		nextAlarm.setText(message);
	}

	/**
	 * Set the NFC tag active alarm.
	 *
	 * @param  activeAlarm  The active alarm to use in conjunction with the NFC tag.
	 */
	private void setNfcTagAlarm(NacAlarm activeAlarm)
	{
		NacNfcTag tag = this.getNfcTag();

		if (tag == null)
		{
			this.mNfcTag = new NacNfcTag(activeAlarm);
		}
		else
		{
			tag.setActiveAlarm(activeAlarm);
		}
	}

	/**
	 * Set the NFC tag action and ID from an Intent.
	 *
	 * @param  nfcIntent  The intent received when scanning an NFC tag.
	 */
	private void setNfcTagIntent(Intent nfcIntent)
	{
		NacNfcTag tag = this.getNfcTag();

		if (tag == null)
		{
			this.mNfcTag = new NacNfcTag(nfcIntent);
		}
		else
		{
			tag.setNfcId(nfcIntent);
			tag.setNfcAction(nfcIntent);
		}
	}

	/**
	 * Setup the alarm card adapter.
	 */
	private void setupAlarmCardAdapter()
	{
		RecyclerView rv = this.getRecyclerView();
		NacCardAdapter adapter = this.getAlarmCardAdapter();
		NacCardTouchHelper touchHelper = this.getAlarmCardTouchHelper();

		adapter.setOnViewHolderBoundListener(this);
		adapter.setOnViewHolderCreatedListener(this);
		touchHelper.attachToRecyclerView(rv);
	}

	/**
	 * Setup the floating action button.
	 */
	private void setupFloatingActionButton()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		FloatingActionButton floatingButton = this.getFloatingActionButton();
		ColorStateList color = ColorStateList.valueOf(shared.getThemeColor());

		//floatingButton.setOnClickListener(this);
		floatingButton.setOnClickListener(this.mFloatingActionButtonListener);
		floatingButton.setBackgroundTintList(color);
	}

	/**
	 * Run the setup when it is the app's first time running.
	 * <p>
	 * If it is not the app's first time running, this does nothing.
	 */
	private void setupForAppFirstRun()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		shared.editAppFirstRun(this, false);
		shared.editAppStartStatistics(false);
		this.addFirstAlarm();
	}

	/**
	 * Setup the Google rating dialog.
	 */
	private boolean setupGoogleRatingDialog()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		return NacRateMyApp.request(this, shared);
	}

	/**
	 * Setup an initial dialog, if any, that need to be shown.
	 */
	private void setupInitialDialogToShow()
	{
		// Get the shared preferences
		NacPermissionRequestManager manager = this.getPermissionRequestManager();

		// Request permissions
		if (manager.count() > 0)
		{
			manager.requestPermissions(this);
		}
		// Show the What's New dialog, but do not show anything else after it is
		// shown
		else if (this.setupWhatsNewDialog())
		{
		}
		// Show the Google in-app rating dialog, but do not show anything else after
		// it is shown
		else if (this.setupGoogleRatingDialog())
		{
		}
	}

	/**
	 * Setup LiveData observers.
	 */
	private void setupLiveDataObservers()
	{
		// Observer is called when list of all alarms changes. Including when the app
		// starts and the list is initially empty
		this.getAlarmViewModel().getAllAlarms().observe(this,
			alarms -> 
				{
					// Setup statistics
					this.setupStatistics(alarms);

					// Merge and sort the alarms if there are none expanded
					if (this.getCardsExpandedCount() == 0)
					{
						this.getAlarmCardAdapterLiveData().mergeSort(alarms);
					}
					// Only merge the alarms when there is at least one alarm expanded
					else
					{
						this.getAlarmCardAdapterLiveData().merge(alarms);
					}

					// Set the next alarm message
					this.setNextAlarmMessage(alarms);

					// Refresh any alarms that will alarm soon
					//this.refreshAlarmsThatWillAlarmSoon();
				});

		this.getAlarmViewModel().getActiveAlarm().observe(this,
			this::prepareActiveAlarm);

		this.getAlarmCardAdapterLiveData().observe(this, this);
	}

	/**
	 * Setup the recycler view.
	 */
	private void setupRecyclerView()
	{
		RecyclerView rv = this.getRecyclerView();
		NacCardAdapter adapter = this.getAlarmCardAdapter();
		int padding = getResources().getDimensionPixelSize(R.dimen.normal);

		Drawable drawable = ContextCompat.getDrawable(this,
			R.drawable.card_divider);
		InsetDrawable insetDrawable = new InsetDrawable(drawable, padding, 0,
			padding, 0);
		DividerItemDecoration divider = new DividerItemDecoration(this,
			LinearLayoutManager.VERTICAL);
		NacLayoutManager layoutManager = new NacLayoutManager(this);

		//divider.setDrawable(drawable);
		divider.setDrawable(insetDrawable);
		rv.addItemDecoration(divider);
		rv.setAdapter(adapter);
		rv.setLayoutManager(layoutManager);
		rv.addOnItemTouchListener(this);
		rv.setHasFixedSize(true);
	}

	/**
	 * Setup the shutdown broadcast receiver.
	 */
	private void setupShutdownBroadcastReceiver()
	{
		NacShutdownBroadcastReceiver receiver = this.getShutdownBroadcastReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);

		if (receiver != null)
		{
			registerReceiver(receiver, filter);
		}
	}

	/**
	 * Setup statistics, and start collecting the data.
	 * <p>
	 * This is only done if this is not the app's first time running and
	 * statistics should be started.
	 *
	 * @param  alarms  List of alarms.
	 */
	private void setupStatistics(List<NacAlarm> alarms)
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		if (shared.getAppStartStatistics())
		{
			NacAlarmStatisticRepository repo = this.getAlarmStatisticRepository();
			long numCreated = repo.getCreatedCount();

			if (numCreated == 0)
			{
				for (NacAlarm a : alarms)
				{
					repo.insertCreated();
				}
			}

			shared.editAppStartStatistics(false);
		}
	}

	/**
	 * Setup the time tick receiver.
	 */
	private void setupTimeTickReceiver()
	{
		BroadcastReceiver receiver = this.getTimeTickReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);

		// Register the receiver
		registerReceiver(receiver, filter);
	}

	/**
	 * Setup the toolbar
	 */
	private void setupToolbar()
	{
		MaterialToolbar toolbar = this.getToolbar();

		toolbar.setOnMenuItemClickListener(this);
	}

	/**
	 * Setup showing the What's New dialog.
	 */
	private boolean setupWhatsNewDialog()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		String version = BuildConfig.VERSION_NAME;
		String prevVersion = shared.getPreviousAppVersion();

		// The current version and previously saved version match. This means there
		// is no update that has occurred. Alternatively, something is wrong with the
		// current version (if it is empty)
		if (version.equals(prevVersion))
		{
			return false;
		}

		// Show the What's New dialog
		this.showWhatsNewDialog();

		return true;
	}

	/**
	 * @return True if the alarm activity should be shown, and False otherwise.
	 */
	private boolean shouldShowAlarmActivity(NacAlarm alarm)
	{
		Intent intent = getIntent();
		return (alarm != null) && !NacNfc.wasScanned(intent);
	}

	/**
	 * Show a snackbar for the alarm.
	 */
	public void showAlarmSnackbar(NacAlarm alarm)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		String message = NacCalendar.getMessageWillRun(shared, alarm);
		String action = getString(R.string.action_alarm_dismiss);

		this.showSnackbar(message, action);
	}

	/**
	 * Show the audio options dialog.
	 */
	public void showAudioOptionsDialog(NacAlarm alarm)
	{
		NacAlarmAudioOptionsDialog dialog = new NacAlarmAudioOptionsDialog();

		dialog.setAlarmId(alarm.getId());
		dialog.setOnAudioOptionClickedListener(NacMainActivity.this);
		dialog.show(getSupportFragmentManager(), NacAlarmAudioOptionsDialog.TAG);
	}

	/**
	 * Show the audio source dialog.
	 */
	public void showAudioSourceDialog()
	{
		NacAudioSourceDialog dialog = new NacAudioSourceDialog();
		NacAlarm alarm = this.getAudioOptionsAlarm();
		String audioSource = alarm.getAudioSource();

		dialog.setDefaultAudioSource(audioSource);
		dialog.setOnAudioSourceSelectedListener(this);
		dialog.show(getSupportFragmentManager(), NacAudioSourceDialog.TAG);
	}

	/**
	 * Show the dismiss early dialog.
	 */
	public void showDismissEarlyDialog()
	{
		NacDismissEarlyDialog dialog = new NacDismissEarlyDialog();
		NacAlarm alarm = this.getAudioOptionsAlarm();
		boolean useDismissEarly = alarm.getShouldUseDismissEarly();
		int index = alarm.getDismissEarlyIndex();

		NacUtility.printf("Index : %d", index);
		dialog.setDefaultShouldDismissEarly(useDismissEarly);
		dialog.setDefaultShouldDismissEarlyIndex(index);
		dialog.setOnDismissEarlyOptionSelectedListener(this);
		dialog.show(getSupportFragmentManager(), NacGraduallyIncreaseVolumeDialog.TAG);
	}

	/**
	 * Show the gradually increase volume dialog.
	 */
	public void showGraduallyIncreaseVolumeDialog()
	{
		NacGraduallyIncreaseVolumeDialog dialog = new NacGraduallyIncreaseVolumeDialog();
		NacAlarm alarm = this.getAudioOptionsAlarm();
		boolean shouldIncrease = alarm.getShouldGraduallyIncreaseVolume();

		dialog.setDefaultShouldGraduallyIncreaseVolume(shouldIncrease);
		dialog.setOnGraduallyIncreaseVolumeListener(this);
		dialog.show(getSupportFragmentManager(), NacGraduallyIncreaseVolumeDialog.TAG);
	}

	/**
	 * Show a snackbar for the next alarm that will run.
	 */
	public void showNextAlarmSnackbar()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacCardAdapter cardAdapter = this.getAlarmCardAdapter();

		List<NacAlarm> alarms = cardAdapter.getCurrentList();
		NacAlarm alarm = NacCalendar.getNextAlarm(alarms);
		String message = NacCalendar.getMessageNextAlarm(shared, alarm);
		String action = getString(R.string.action_alarm_dismiss);

		this.showSnackbar(message, action);
	}

	/**
	 * Show the saved NFC tag ID of the given alarm.
	 */
	public void showNfcTagId(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		Locale locale = Locale.getDefault();
		String id = alarm.getNfcTagId();
		String message;
		
		if (!id.isEmpty())
		{
			String nfcId = getString(R.string.message_show_nfc_tag_id);
			message = String.format(locale, "%1$s %2$s", nfcId, id);
		}
		else
		{
			String anyNfc = getString(R.string.message_any_nfc_tag_id);
			message = String.format(locale, "%1$s", anyNfc);
		}

		NacUtility.quickToast(this, message);
	}

	/**
	 * Show the restrict volume dialog.
	 */
	public void showRestrictVolumeDialog()
	{
		NacRestrictVolumeDialog dialog = new NacRestrictVolumeDialog();
		NacAlarm alarm = this.getAudioOptionsAlarm();
		boolean shouldRestrict = alarm.getShouldRestrictVolume();

		dialog.setDefaultShouldRestrictVolume(shouldRestrict);
		dialog.setOnRestrictVolumeListener(this);
		dialog.show(getSupportFragmentManager(), NacRestrictVolumeDialog.TAG);
	}

	/**
	 * @see #showSnackbar(String, String, View.OnClickListener)
	 */
	private void showSnackbar(String message, String action)
	{
		this.showSnackbar(message, action, null);
	}

	/**
	 * Create a snackbar message.
	 */
	private void showSnackbar(String message, String action,
		View.OnClickListener listener)
	{
		NacSnackbar snackbar = this.getSnackbar();
		snackbar.show(message, action, listener, true);
	}

	/**
	 * Show the text-to-speech dialog.
	 */
	public void showTextToSpeechDialog()
	{
		NacTextToSpeechDialog dialog = new NacTextToSpeechDialog();
		NacAlarm alarm = this.getAudioOptionsAlarm();
		boolean useTts = alarm.getShouldUseTts();
		int freq = alarm.getTtsFrequency();

		dialog.setDefaultUseTts(useTts);
		dialog.setDefaultTtsFrequency(freq);
		dialog.setOnTextToSpeechOptionsSelectedListener(this);
		dialog.show(getSupportFragmentManager(), NacTextToSpeechDialog.TAG);
	}

	/**
	 * Show a snackbar for the updated alarm.
	 * <p>
	 * If this alarm is disabled, a snackbar for the next alarm will be shown.
	 */
	public void showUpdatedAlarmSnackbar(NacAlarm alarm)
	{
		if (alarm.isEnabled())
		{
			this.showAlarmSnackbar(alarm);
		}
		else
		{
			this.showNextAlarmSnackbar();
		}
	}

	/**
	 * Show the What's New dialog.
	 */
	public void showWhatsNewDialog()
	{
		NacWhatsNewDialog dialog = new NacWhatsNewDialog();

		dialog.setOnReadWhatsNewListener(this);
		dialog.show(getSupportFragmentManager(), NacWhatsNewDialog.TAG);
	}

	/**
	 * Update the notification.
	 * <p>
	 * TODO: Check if race condition with this being called after submitList?
	 * Should I just pass a list of alarms to this method?
	 */
	public void updateUpcomingNotification(List<NacAlarm> alarms)
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		if (shared.getUpcomingAlarmNotification())
		{
			NacUpcomingAlarmNotification notification =
				new NacUpcomingAlarmNotification(this);

			notification.setAlarmList(alarms);
			notification.show();
		}
	}

	/**
	 * @see #updateUpcomingNotification(List)
	 */
	public void updateUpcomingNotification()
	{
		NacCardAdapter cardAdapter = this.getAlarmCardAdapter();
		List<NacAlarm> alarms = cardAdapter.getCurrentList();

		this.updateUpcomingNotification(alarms);
	}

	/**
	 * Verify that the card is measured.
	 * <p>
	 * If a card has already been measured, this does nothing.
	 */
	private void verifyCardIsMeasured(NacCardHolder card)
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		if (shared.getCardIsMeasured())
		{
			return;
		}

		int[] heights = new int[3];

		card.measureCard(heights);
		shared.editCardHeightCollapsed(heights[0]);
		shared.editCardHeightCollapsedDismiss(heights[1]);
		shared.editCardHeightExpanded(heights[2]);
		shared.editCardIsMeasured(true);
	}

	/**
	 * @return True if an NFC tag was scanned to dismiss an alarm, and False
	 *         otherwise. This is to say that if an NFC tag was scanned for the
	 *         dialog, this would return False.
	 */
	private boolean wasNfcScannedForActiveAlarm(Intent intent)
	{
		return NacNfc.wasScanned(intent) && !this.wasNfcScannedForDialog(intent);
	}

	/**
	 * @return True if an NFC tag was scanned while the Scan NFC Tag dialog was
	 *         open, and False otherwise.
	 */
	private boolean wasNfcScannedForDialog(Intent intent)
	{
		NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();
		return (dialog != null) && NacNfc.wasScanned(intent);
	}

}
