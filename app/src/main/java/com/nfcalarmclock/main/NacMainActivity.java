package com.nfcalarmclock.main;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.textview.MaterialTextView;
import com.nfcalarmclock.activealarm.NacActiveAlarmService;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.alarm.NacAlarmViewModel;
import com.nfcalarmclock.audiooptions.NacAlarmAudioOptionsDialog;
import com.nfcalarmclock.audiosource.NacAudioSourceDialog;
import com.nfcalarmclock.card.NacCardAdapter;
import com.nfcalarmclock.card.NacCardAdapterLiveData;
import com.nfcalarmclock.card.NacCardHolder;
import com.nfcalarmclock.card.NacCardTouchHelper;
import com.nfcalarmclock.dismissearly.NacDismissEarlyDialog;
import com.nfcalarmclock.graduallyincreasevolume.NacGraduallyIncreaseVolumeDialog;
import com.nfcalarmclock.media.NacMedia;
import com.nfcalarmclock.mediapicker.NacMediaActivity;
import com.nfcalarmclock.nfc.NacNfc;
import com.nfcalarmclock.nfc.NacNfcTag;
import com.nfcalarmclock.nfc.NacScanNfcTagDialog;
import com.nfcalarmclock.permission.NacScheduleExactAlarmPermissionDialog;
import com.nfcalarmclock.R;
import com.nfcalarmclock.ratemyapp.NacRateMyApp;
import com.nfcalarmclock.restrictvolume.NacRestrictVolumeDialog;
import com.nfcalarmclock.scheduler.NacScheduler;
import com.nfcalarmclock.settings.NacSettingsActivity;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.snackbar.NacSnackbar;
import com.nfcalarmclock.shutdown.NacShutdownBroadcastReceiver;
import com.nfcalarmclock.statistics.NacAlarmStatisticRepository;
import com.nfcalarmclock.system.NacCalendar;
import com.nfcalarmclock.system.NacContext;
import com.nfcalarmclock.system.NacIntent;
import com.nfcalarmclock.tts.NacTextToSpeechDialog;
import com.nfcalarmclock.upcomingalarm.NacUpcomingAlarmNotification;
import com.nfcalarmclock.util.dialog.NacDialog;
import com.nfcalarmclock.util.NacUtility;
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
		//View.OnClickListener,
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
        NacDialog.OnCancelListener,
		NacDialog.OnDismissListener,
        NacAlarmAudioOptionsDialog.OnAudioOptionClickedListener,
        NacAudioSourceDialog.OnAudioSourceSelectedListener,
		NacDismissEarlyDialog.OnDismissEarlyOptionSelectedListener,
		NacGraduallyIncreaseVolumeDialog.OnGraduallyIncreaseVolumeListener,
		NacRestrictVolumeDialog.OnRestrictVolumeListener,
		NacTextToSpeechDialog.OnTextToSpeechOptionsSelectedListener,
		NacWhatsNewDialog.OnReadWhatsNewListener,
		NacScheduleExactAlarmPermissionDialog.OnPermissionRequestListener
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
	 *
	 * Live data from the view model cannot be sorted, hence the need for this.
	 */
	private NacCardAdapterLiveData mAlarmCardAdapterLiveData;

	///**
	// * A currently active alarm.
	// */
	//private NacAlarm mActiveAlarm;

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
	 * Listener for when the floating action button is clicked.
	 */
	private final View.OnClickListener mFloatingActionButtonListener =
			view -> {
				NacSharedPreferences shared = getSharedPreferences();
				NacSharedConstants cons = getSharedConstants();
				NacCardAdapter adapter = getAlarmCardAdapter();
				int size = adapter.getItemCount();

				// Haptic feedback so that the user knows the action was received
				view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

				// Max number of alarms reached
				if ((size+1) > cons.getMaxAlarms())
				{
					NacUtility.quickToast(NacMainActivity.this,
						cons.getErrorMessageMaxAlarms());
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

				if (lastAction.wasCopy())
				{
					deleteAlarm(alarm);
				}
				else if (lastAction.wasDelete())
				{
					restoreAlarm(alarm);
				}
				else if (lastAction.wasRestore())
				{
					deleteAlarm(alarm);
				}
            };

	/**
	 * Receiver for the time tick intent. This is called when the time increments
	 * every minute.
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

		long id = this.getAlarmViewModel().insert(this, alarm);

		if (alarm.getId() <= 0)
		{
			alarm.setId(id);
		}

		this.getRecentlyAddedAlarmIds().add(id);
		this.getAlarmStatisticRepository().insertCreated();
	}

	/**
	 * Add the first alarm, when the app is first run.
	 */
	private void addFirstAlarm()
	{
		// Create the alarm
		NacAlarm alarm = new NacAlarm.Builder()
			.setId(0)
			.setIsEnabled(true)
			.setHour(8)
			.setMinute(0)
			.setDays(NacCalendar.Days.valueToDays(62))
			.setRepeat(true)
			.setVibrate(true)
			.setUseNfc(false)
			.setNfcTagId("")
			.setMediaType(NacMedia.TYPE_NONE)
			.setMediaPath("")
			.setMediaTitle("")
			.setVolume(75)
			.setAudioSource("Media")
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
		if (dialog != null)
		{
			dialog.getAlertDialog().dismiss();
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
				unregisterReceiver(receiver);
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
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
			unregisterReceiver(receiver);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * TODO: Catch exceptions properly
	 */
	public void copyAlarm(NacAlarm alarm)
	{
		NacSharedConstants cons = this.getSharedConstants();
		String message = cons.getMessageAlarmCopy();
		String action = cons.getActionUndo();

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

		NacSharedConstants cons = this.getSharedConstants();
		String message = cons.getMessageAlarmDelete();
		String action = cons.getActionUndo();

		this.getAlarmViewModel().delete(this, alarm);
		this.getAlarmStatisticRepository().insertDeleted(alarm);
		this.getLastAlarmCardAction().set(alarm, NacLastAlarmCardAction.Type.DELETE);
		this.showSnackbar(message, action, this.mOnSwipeSnackbarActionListener);
	}

	/**
	 * Attempt to dismiss the first active alarm found.
	 *
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
	 * @return The shared constants.
	 */
	private NacSharedConstants getSharedConstants()
	{
		return this.getSharedPreferences().getConstants();
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
	 * Uncheck the NFC button when the dialog is canceled.
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	{
		RecyclerView rv = this.getRecyclerView();
		NacAlarm alarm = (NacAlarm) dialog.getData();

		if (alarm != null)
		{
			long id = alarm.getId();
			NacCardHolder cardHolder = (NacCardHolder) rv.findViewHolderForItemId(id);

			if (cardHolder != null)
			{
				cardHolder.getNfcButton().setChecked(false);
				cardHolder.doNfcButtonClick();
			}
		}

		this.cleanupScanNfcTagDialog();
		return true;
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
		if (!alarm.shouldUseNfc())
		{
			return;
		}

		NacScanNfcTagDialog dialog = new NacScanNfcTagDialog();
		this.mScanNfcTagDialog = dialog;

		dialog.build(this);
		dialog.saveData(alarm);
		dialog.addOnCancelListener(this);
		dialog.addOnDismissListener(this);
		dialog.show();
	}

	/**
	 * Alarm list has changed.
	 *
	 * TODO: There is a race condition between snoozing an alarm, writing to the
	 *     database, and refreshing the main activity.
	 */
	@Override
	public void onChanged(List<NacAlarm> alarms)
	{
		RecyclerView rv = this.getRecyclerView();

		this.setupForAppFirstRun();
		this.updateUpcomingNotification(alarms);
		this.getAlarmCardAdapter().storeIndicesOfExpandedCards(rv);
		this.getAlarmCardAdapter().submitList(alarms);
		this.setupRefreshMainActivity();
	}

	/**
	 * Called when an alarm card was swiped to copy.
	 *
	 * @param  index  The index of the alarm card.
	 */
	@Override
	public void onCopySwipe(NacAlarm alarm, int index)
	{
		NacSharedConstants cons = this.getSharedConstants();
		NacCardAdapter adapter = this.getAlarmCardAdapter();
		NacCardHolder card = this.getAlarmCardAt(index);
		int size = adapter.getItemCount();

		// Haptic feedback so that the user knows the action was received
		card.getRoot().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

		// Reset the view on the alarm that was swiped
		adapter.notifyItemChanged(index);

		// Max number of alarms reached
		if ((size+1) > cons.getMaxAlarms())
		{
			NacUtility.quickToast(this, cons.getErrorMessageMaxAlarms());
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

		Intent intent = getIntent();
		View root = findViewById(R.id.activity_main);
		this.mToolbar = findViewById(R.id.tb_top_bar);
		this.mNextAlarmTextView = findViewById(R.id.tv_next_alarm);
		this.mFloatingActionButton = findViewById(R.id.fab_add_alarm);
		this.mRecyclerView = findViewById(R.id.rv_alarm_list);
		this.mSharedPreferences = new NacSharedPreferences(this);
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

		// Show the dialog to schedule an exact alarm
		if (this.shouldRequestScheduleExactAlarmPermission())
		{
			this.showScheduleExactAlarmPermissionDialog();
		}

		// Disable the activity alias so that tapping an NFC tag will not do anything
		PackageManager pm = getPackageManager();
		String packageName = getPackageName();
		String aliasName = packageName + ".main.NacMainAliasActivity";
		ComponentName componentName = new ComponentName(this, aliasName);

		pm.setComponentEnabledSetting(componentName,
			PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
			PackageManager.DONT_KILL_APP);
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
		card.getRoot().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

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
	 * Set the default (empty) NFC tag ID.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacSharedConstants cons = this.getSharedConstants();
		NacAlarm alarm = (NacAlarm) dialog.getData();

		alarm.setNfcTagId("");
		this.getAlarmViewModel().update(this, alarm);
		this.cleanupScanNfcTagDialog();
		NacUtility.quickToast(this, cons.getMessageNfcRequired());
		return true;
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

			if (snackbar.canDismiss())
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
			Intent settingsIntent = new Intent(this, NacSettingsActivity.class);
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
			NacSharedConstants cons = this.getSharedConstants();
			NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();

			// Save the NFC tag ID and show a toast
			this.saveNfcTagId(intent);
			NacUtility.quickToast(this, cons.getMessageNfcRequired());

			// Close the dialog
			if (dialog != null)
			{
				dialog.saveData(null);
				dialog.cancel();
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
	 * Called when a permission request was canceled.
	 */
	public void onPermissionRequestCancel(String permission)
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		// Set the shared preference indicating that the permission was requested
		shared.editWasScheduleExactAlarmPermissionRequested(true);

		// Setup the initial dialogs, if any, that need to be shown
		this.setupInitialDialogToShow();
	}

	/**
	 * Called when a permission request is done.
	 */
	public void onPermissionRequestDone(String permission)
	{
		// Do not do anything if the Android version is not correct
		if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
				|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
		{
			return;
		}

		// Set the shared preference indicating that the permission was requested
		NacSharedPreferences shared = this.getSharedPreferences();
		shared.editWasScheduleExactAlarmPermissionRequested(true);

		// Start the intent to facilitate the user enabling the permission
		Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
		startActivity(intent);
	}

	/**
	 * Called when the What's New dialog has been read.
	 */
	@Override
	public void onReadWhatsNew()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacSharedConstants cons = this.getSharedConstants();
		String version = cons.getAppVersion();

		// Set the previous app version as the current version. This way, the What's
		// New dialog does not show again
		shared.editPreviousAppVersion(version);

		// Refresh all the scheduled alarms
		NacScheduler.refreshAll(this);
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

		this.setIsActivityShown(true);
		this.setupTimeTickReceiver();
		this.setNextAlarmMessage();
		//this.refreshAlarmsThatWillAlarmSoon();
		this.setupRefreshMainActivity();
		// Will have to redraw colors here?
		this.setupFloatingActionButton();
		this.setupInitialDialogToShow();
		this.addSetAlarmFromIntent();
		this.setupShutdownBroadcastReceiver();
		NacNfc.start(this);
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
	 *
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
			if (a.willAlarmSoon() && card.shouldRefreshDismissView())
			{
				// Refresh the alarm
				adapter.notifyItemChanged(i);
			}
		}
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

		NacSharedConstants cons = this.getSharedConstants();
		String message = cons.getMessageAlarmRestore();
		String action = cons.getActionUndo();

		this.getLastAlarmCardAction().set(alarm, NacLastAlarmCardAction.Type.RESTORE);
		this.getAlarmViewModel().insert(this, alarm);
		this.showSnackbar(message, action, this.mOnSwipeSnackbarActionListener);
	}

	/**
	 * Save the scanned NFC tag ID.
	 */
	private void saveNfcTagId(Intent intent)
	{
		if (!this.wasNfcScannedForDialog(intent))
		{
			return;
		}

		Tag nfcTag = NacNfc.getTag(intent);
		if (nfcTag == null)
		{
			return;
		}

		NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();
		NacAlarm alarm = (NacAlarm) dialog.getData();
		String id = NacNfc.parseId(nfcTag);

		alarm.setNfcTagId(id);
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
	 *
	 * If it is not the app's first time running, this does nothing.
	 */
	private void setupForAppFirstRun()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		if (!shared.getAppFirstRun())
		{
			return;
		}

		shared.editAppFirstRun(false);
		shared.editAppStartStatistics(false);
		this.addFirstAlarm();
	}

	/**
	 * Setup the Google rating dialog.
	 */
	private boolean setupGoogleRatingDialog()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		return NacRateMyApp.request(shared);
	}

	/**
	 * Setup an initial dialog, if any, that need to be shown.
	 */
	private void setupInitialDialogToShow()
	{
		// Do not show any of these dialogs if requesting a permission
		if (this.shouldRequestScheduleExactAlarmPermission())
		{
			return;
		}

		// Show the What's New dialog, but do not show anything else after it is
		// shown
		if (this.setupWhatsNewDialog())
		{
			return;
		}
		// Show the Google in-app rating dialog, but do not show anything else after
		// it is shown
		else if (this.setupGoogleRatingDialog())
		{
			return;
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
	 * Setup whether the main activity should be refreshed or not.
	 */
	private void setupRefreshMainActivity()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		if (shared.getShouldRefreshMainActivity())
		{
			shared.editShouldRefreshMainActivity(false);
			recreate();
		}
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
	 *
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
		NacSharedConstants cons = this.getSharedConstants();
		String version = cons.getAppVersion();
		String prevVersion = shared.getPreviousAppVersion();

		// The current version and previously saved version match. This means there
		// is no update that has occurred. Alternatively, something is wrong with the
		// current version (if it is empty)
		if (version.isEmpty() || version.equals(prevVersion))
		{
			return false;
		}

		// Show the What's New dialog
		this.showWhatsNewDialog();

		return true;
	}

	/**
	 * @return True if the app should request the permission to be able to
	 *         schedule exact alarms, and False otherwise.
	 */
	private boolean shouldRequestScheduleExactAlarmPermission()
	{
		// Schedule Exact Alarms permission is only applicable to API 31 and 32.
		if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
			|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
		{
			return false;
		}

		AlarmManager alarmManager = (AlarmManager) getSystemService(
			Context.ALARM_SERVICE);
		NacSharedPreferences shared = this.getSharedPreferences();

		// Request permission when unable to schedule exact alarms, and this
		// permission has not been requested yet
		return !alarmManager.canScheduleExactAlarms()
			&& !shared.getWasScheduleExactAlarmPermissionRequested();
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
		NacSharedConstants cons = this.getSharedConstants();
		NacSharedPreferences shared = this.getSharedPreferences();
		String message = NacCalendar.getMessageWillRun(shared, alarm);
		String action = cons.getActionDismiss();

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
		boolean useDismissEarly = alarm.shouldUseDismissEarly();
		int index = alarm.getDismissEarlyIndex();

		dialog.setDefaultUseDismissEarly(useDismissEarly);
		dialog.setDefaultDismissEarlyIndex(index);
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
		NacSharedConstants cons = this.getSharedConstants();
		NacSharedPreferences shared = this.getSharedPreferences();
		NacCardAdapter cardAdapter = this.getAlarmCardAdapter();

		List<NacAlarm> alarms = cardAdapter.getCurrentList();
		NacAlarm alarm = NacCalendar.getNextAlarm(alarms);
		String message = NacCalendar.getMessageNextAlarm(shared, alarm);
		String action = cons.getActionDismiss();

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

		NacSharedConstants cons = this.getSharedConstants();
		Locale locale = Locale.getDefault();
		String id = alarm.getNfcTagId();
		String message;
		
		if (!id.isEmpty())
		{
			message = String.format(locale, "%1$s %2$s", cons.getMessageShowNfcTagId(),
				id);
		}
		else
		{
			message = String.format(locale, "%1$s", cons.getMessageAnyNfcTagId());
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
	 * Show the dialog.
	 */
	@RequiresApi(api = Build.VERSION_CODES.S)
	public void showScheduleExactAlarmPermissionDialog()
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
		{
			return;
		}

		NacScheduleExactAlarmPermissionDialog dialog =
			new NacScheduleExactAlarmPermissionDialog();

		dialog.setOnPermissionRequestListener(this);
		dialog.show(getSupportFragmentManager(),
			NacScheduleExactAlarmPermissionDialog.TAG);
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
		boolean useTts = alarm.shouldUseTts();
		int freq = alarm.getTtsFrequency();

		dialog.setDefaultUseTts(useTts);
		dialog.setDefaultTtsFrequency(freq);
		dialog.setOnTextToSpeechOptionsSelectedListener(this);
		dialog.show(getSupportFragmentManager(), NacTextToSpeechDialog.TAG);
	}

	/**
	 * Show a snackbar for the updated alarm.
	 *
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
	 *
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
	 * @see #updateUpcomingNotification(List<NacAlarm>)
	 */
	public void updateUpcomingNotification()
	{
		NacCardAdapter cardAdapter = this.getAlarmCardAdapter();
		List<NacAlarm> alarms = cardAdapter.getCurrentList();

		this.updateUpcomingNotification(alarms);
	}

	/**
	 * Verify that the card is measured.
	 *
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
