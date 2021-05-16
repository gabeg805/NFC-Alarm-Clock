package com.nfcalarmclock;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
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
		MenuItem.OnMenuItemClickListener,
		RecyclerView.OnItemTouchListener,
		NacCardTouchHelper.OnSwipedListener,
		NacAlarmCardAdapter.OnViewHolderBoundListener,
		NacAlarmCardAdapter.OnViewHolderCreatedListener,
		NacCardHolder.OnCardCollapsedListener,
		NacCardHolder.OnCardDeleteClickedListener,
		NacCardHolder.OnCardExpandedListener,
		NacCardHolder.OnCardMediaClickedListener,
		NacCardHolder.OnCardUpdatedListener,
		NacCardHolder.OnCardUseNfcChangedListener,
		NacDialog.OnCancelListener,
		NacDialog.OnDismissListener
{

	/**
	 * Wait time between a notification posting and running the alarm activity
	 * for that notification.
	 */
	private static final long SHOW_ALARM_ACTIVITY_DELAY = 1000;

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

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
	private NacAlarmCardAdapter mAlarmCardAdapter;

	/**
	 * Scan an NFC tag dialog.
	 */
	private NacScanNfcTagDialog mScanNfcTagDialog;

	/**
	 * Shutdown broadcast receiver.
	 */
	private NacShutdownBroadcastReceiver mShutdownBroadcastReceiver;

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
	private NacAlarmCardAdapterLiveData mAlarmCardAdapterLiveData;

	/**
	 * A currently active alarm.
	 */
	private NacAlarm mActiveAlarm;

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
	 * Listener for when the floating action button is clicked.
	 */
	private final View.OnClickListener mFloatingActionButtonListener =
		new View.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				NacSharedPreferences shared = getSharedPreferences();
				NacAlarm alarm = new NacAlarm.Builder(shared).build();
				long id = getAlarmViewModel().insert(NacMainActivity.this, alarm);

				getRecentlyAddedAlarmIds().add(id);
				view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			}
		};


	/**
	 * Capture the click event on the Snackbar button.
	 */
	private final View.OnClickListener mOnSwipeSnackbarActionListener =
		new View.OnClickListener() {
			@Override
			public void onClick(View view)
			{
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
				else
				{
				}
			}
		};

	/**
	 * Add an alarm that was created from the SET_ALARM intent.
	 */
	private void addSetAlarmFromIntent()
	{
		Intent intent = getIntent();
		NacAlarmCardAdapter cardAdapter = this.getAlarmCardAdapter();
		NacAlarm alarm = NacIntent.getSetAlarm(this, intent);

		if (alarm != null)
		{
			long id = this.getAlarmViewModel().insert(this, alarm);
			this.getRecentlyAddedAlarmIds().add(id);
		}
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
			unregisterReceiver(receiver);
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

		long id = this.getAlarmViewModel().copy(alarm);
		if (id >= 0)
		{
			alarm.setId(id);
		}
		else
		{
			NacUtility.printf("UNABLE TO get Id of copied row!");
		}

		this.getLastAlarmCardAction().set(alarm, NacLastAlarmCardAction.Type.COPY);
		this.showSnackbar(message, action, this.mOnSwipeSnackbarActionListener);
	}

	/**
	 */
	public void deleteAlarm(NacAlarm alarm)
	{
		NacSharedConstants cons = this.getSharedConstants();
		String message = cons.getMessageAlarmDelete();
		String action = cons.getActionUndo();

		this.getAlarmViewModel().delete(this, alarm);
		this.getLastAlarmCardAction().set(alarm, NacLastAlarmCardAction.Type.DELETE);
		this.showSnackbar(message, action, this.mOnSwipeSnackbarActionListener);
	}

	/**
	 * Attempt to dismiss the first active alarm found.
	 *
	 * If unable to dismiss the alarm, the alarm activity is shown.
	 */
	private void dismissActiveAlarm(Intent intent)
	{
		NacAlarm alarm = this.getActiveAlarm();
		if (alarm == null)
		{
			return;
		}

		boolean success = NacContext.dismissForegroundServiceFromNfcScan(this,
			intent, alarm);

		if (success)
		{
			recreate();
		}
		else
		{
			this.showAlarmActivity(alarm);
		}
	}

	/**
	 * @return A currently active alarm.
	 */
	private NacAlarm getActiveAlarm()
	{
		return this.mActiveAlarm;
	}

	/**
	 * @return The alarm card adapter.
	 */
	private NacAlarmCardAdapter getAlarmCardAdapter()
	{
		return this.mAlarmCardAdapter;
	}

	/**
	 * @return Mutable live data that is used to submit data to the alarm card
	 *     adapter.
	 */
	private NacAlarmCardAdapterLiveData getAlarmCardAdapterLiveData()
	{
		return this.mAlarmCardAdapterLiveData;
	}

	/**
	 * @return The alarm card touch helper.
	 */
	private NacCardTouchHelper getAlarmCardTouchHelper()
	{
		return this.mAlarmCardTouchHelper;
	}

	/**
	 * @return The alarm view model.
	 */
	private NacAlarmViewModel getAlarmViewModel()
	{
		return this.mAlarmViewModel;
	}

	/**
	 * @return A list of all the alarm view holders.
	 */
	private List<NacCardHolder> getAllAlarmViewHolders()
	{
		RecyclerView rv = this.getRecyclerView();
		NacAlarmCardAdapter adapter = this.getAlarmCardAdapter();
		int size = adapter.getItemCount();
		List<NacCardHolder> viewHolders = new ArrayList<>();

		for (int i=0; i < size; i++)
		{
			NacCardHolder card = (NacCardHolder) rv.findViewHolderForAdapterPosition(i);
			viewHolders.add(card);
		}

		return viewHolders;
	}

	/**
	 * @return The number of alarm cards that are expanded.
	 */
	private int getCardsExpandedCount()
	{
		RecyclerView rv = this.getRecyclerView();
		NacAlarmCardAdapter adapter = this.getAlarmCardAdapter();

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
	 * @return True if the activity is shown, and False otherwise.
	 */
	private boolean isActivityShown()
	{
		return this.mIsActivityShown;
	}

	/**
	 * Uncheck the NFC button when the dialog is canceled.
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	{
		RecyclerView rv = this.getRecyclerView();
		NacAlarm alarm = (NacAlarm) dialog.getData();
		long id = alarm.getId();
		NacCardHolder cardHolder = (NacCardHolder) rv.findViewHolderForItemId(id);

		if (cardHolder != null)
		{
			cardHolder.getNfcButton().setChecked(false);
			cardHolder.doNfcButtonClick();
		}

		this.cleanupScanNfcTagDialog();
		return true;
	}

	/**
	 * Called when the alarm card is collapsed.
	 */
	@Override
	public void onCardCollapsed(NacCardHolder holder, NacAlarm alarm)
	{
		List<Long> updatedAlarms = this.getRecentlyUpdatedAlarmIds();
		long id = alarm.getId();

		if (this.getCardsExpandedCount() == 0)
		{
			this.getAlarmCardAdapterLiveData().sort();
		}

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
		if (holder.isCollapsed())
		{
			this.showUpdatedAlarmSnackbar(alarm);
			holder.highlight();
		}
		else
		{
			long id = alarm.getId();
			this.getRecentlyUpdatedAlarmIds().add(id);
		}

		this.getAlarmViewModel().update(this, alarm);
		this.updateUpcomingNotification();
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
	 */
	@Override
	public void onChanged(List<NacAlarm> alarms)
	{
		RecyclerView rv = this.getRecyclerView();

		this.setupForAppFirstRun(alarms);
		this.getAlarmCardAdapter().storeIndicesOfExpandedCards(rv);
		this.getAlarmCardAdapter().submitList(alarms);
		this.updateUpcomingNotification();
	}

	/**
	 * Called when an alarm card was swiped to copy.
	 *
	 * @param  index  The index of the alarm card.
	 */
	@Override
	public void onCopySwipe(NacAlarm alarm, int index)
	{
		RecyclerView rv = this.getRecyclerView();
		NacAlarmCardAdapter adapter = this.getAlarmCardAdapter();
		NacCardHolder holder = (NacCardHolder) rv.findViewHolderForAdapterPosition(index);
		int size = adapter.getItemCount();

		this.getAlarmCardTouchHelper().getCallback().clearView(rv, holder);
		this.getLastAlarmCardAction().setIndex(size);
		this.copyAlarm(alarm);
	}

	/**
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);

		Intent intent = getIntent();
		if (this.wasNfcScannedForAlarm(intent))
		{
			this.dismissActiveAlarm(intent);
		}

		View root = findViewById(R.id.activity_main);
		this.mFloatingActionButton = findViewById(R.id.fab_add_alarm);
		this.mRecyclerView = findViewById(R.id.content_alarm_list);
		this.mSharedPreferences = new NacSharedPreferences(this);
		this.mAlarmCardAdapter = new NacAlarmCardAdapter();
		this.mAlarmCardTouchHelper = new NacCardTouchHelper(this);
		this.mAlarmCardAdapterLiveData = new NacAlarmCardAdapterLiveData();
		this.mAlarmViewModel = new ViewModelProvider(this).get(NacAlarmViewModel.class);
		this.mActiveAlarm = null;
		this.mRecentlyAddedAlarmIds = new ArrayList<>();
		this.mRecentlyUpdatedAlarmIds = new ArrayList<>();
		this.mLastAlarmCardAction = new NacLastAlarmCardAction();
		this.mShutdownBroadcastReceiver = new NacShutdownBroadcastReceiver();
		this.mSnackbar = new NacSnackbar(root);
		this.mScanNfcTagDialog = null;

		this.getSharedPreferences().editCardIsMeasured(false);
		this.setupLiveDataObservers();
		this.setupAlarmCardAdapter();
		this.setupRecyclerView();
	}

	/**
	 * Create the context menu.
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
		ContextMenuInfo menuInfo)
	{
		// Hopefully don't need this anymore due to onViewHolderCreatedListener
		//if (menu.size() > 0)
		//{
		//	return;
		//}

		this.mLastCardClicked = view;

		getMenuInflater().inflate(R.menu.menu_card, menu);

		for (int i=0; i < menu.size(); i++)
		{
			MenuItem item = menu.getItem(i);
			item.setOnMenuItemClickListener(this);
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
		this.getLastAlarmCardAction().setIndex(index);
		this.deleteAlarm(alarm);
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
		RecyclerView rv = this.getRecyclerView();
		View view = this.mLastCardClicked;
		NacCardHolder holder = (NacCardHolder) rv.findContainingViewHolder(view);
		int id = item.getItemId();

		if (holder != null)
		{
			NacAlarm alarm = holder.getAlarm();

			if (id == R.id.menu_show_next_alarm)
			{
				this.showAlarmSnackbar(alarm);
			}
			else if (id == R.id.menu_show_nfc_tag_id)
			{
				this.showNfcTagId(alarm);
			}
		}

		this.mLastCardClicked = null;
		return true;
	}

	/**
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		if (this.wasNfcScannedForDialog(intent))
		{
			NacScanNfcTagDialog dialog = this.getScanNfcTagDialog();
			NacSharedConstants cons = new NacSharedConstants(this);

			this.saveNfcTagId(intent);
			dialog.saveData(null);
			dialog.cancel();
			NacUtility.quickToast(this, cons.getMessageNfcRequired());
		}
		else if (this.wasNfcScannedForAlarm(intent))
		{
			this.dismissActiveAlarm(intent);
		}
	}

	/**
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if (id == R.id.menu_settings)
		{
			Intent settingsIntent = new Intent(this, NacSettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		}
		else if (id == R.id.menu_show_next_alarm)
		{
			NacAlarmCardAdapter cardAdapter = this.getAlarmCardAdapter();
			this.showNextAlarmSnackbar();
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 */
	@Override
	protected void onPause()
	{
		super.onPause();

		this.setIsActivityShown(false);
		this.cleanupShutdownBroadcastReceiver();
		NacNfc.stop(this);
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
	protected void onResume()
	{
		super.onResume();

		this.setIsActivityShown(true);
		this.setupRefreshMainActivity();

		// Will have to redraw colors here?

		this.setupFloatingActionButton();
		this.setupGoogleRatingDialog();
		this.addSetAlarmFromIntent();
		this.setupShutdownBroadcastReceiver();
		NacNfc.start(this);
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
		this.verifyCardIsMeasured(card);

		NacAlarmCardAdapter adapter = this.getAlarmCardAdapter();
		NacAlarm alarm = adapter.getAlarmAt(index);
		List<Long> addedAlarms = this.getRecentlyAddedAlarmIds();
		long id = alarm.getId();

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
	public void onViewHolderCreated(NacCardHolder card)
	{
		card.setOnCardCollapsedListener(this);
		card.setOnCardDeleteClickedListener(this);
		card.setOnCardExpandedListener(this);
		card.setOnCardMediaClickedListener(this);
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
		if (!this.isActivityShown())
		{
			return;
		}

		this.mActiveAlarm = alarm;

		if (this.shouldShowAlarmActivity(alarm))
		{
			//NacSharedPreferences shared = this.getSharedPreferences();
			//Remove this setting: shared.getPreventAppFromClosing()?
			this.showAlarmActivity(alarm);
		}
	}

	/**
	 */
	public void restoreAlarm(NacAlarm alarm)
	{
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
	 * Setup the alarm card adapter.
	 */
	private void setupAlarmCardAdapter()
	{
		RecyclerView rv = this.getRecyclerView();
		NacAlarmCardAdapter adapter = this.getAlarmCardAdapter();
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
	private void setupForAppFirstRun(List<NacAlarm> alarms)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int size = alarms.size();

		if (!shared.getAppFirstRun() || (size == 0))
		{
			return;
		}

		NacScheduler.updateAll(this, alarms);
		shared.editAppFirstRun(false);
	}

	/**
	 * Setup the Google rating dialog.
	 */
	private void setupGoogleRatingDialog()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		if (shared.isRateMyAppRated())
		{
			return;
		}

		if (shared.isRateMyAppLimit())
		{
			NacRateMyAppDialog dialog = new NacRateMyAppDialog();
			dialog.build(this);
			dialog.show();
		}
		else
		{
			shared.incrementRateMyApp();
		}
	}

	/**
	 * Setup LiveData observers.
	 */
	private void setupLiveDataObservers()
	{
		this.getAlarmViewModel().getAllAlarms().observe(this,
			alarms -> 
				{
					if (this.getCardsExpandedCount() == 0)
					{
						this.getAlarmCardAdapterLiveData().mergeSort(alarms);
					}
					else
					{
						this.getAlarmCardAdapterLiveData().merge(alarms);
					}
				});

		this.getAlarmViewModel().getActiveAlarm().observe(this,
			alarm -> this.prepareActiveAlarm(alarm));

		this.getAlarmCardAdapterLiveData().observe(this, this);
	}

	/**
	 * Setup the recycler view.
	 */
	private void setupRecyclerView()
	{
		RecyclerView rv = this.getRecyclerView();
		NacAlarmCardAdapter adapter = this.getAlarmCardAdapter();
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
	 * @return True if the alarm activity should be shown, and False otherwise.
	 */
	private boolean shouldShowAlarmActivity(NacAlarm alarm)
	{
		Intent intent = getIntent();
		return (alarm != null) && !NacNfc.wasScanned(intent);
	}

	/**
	 * Show the alarm activity.
	 *
	 * @param  alarm  The alarm that should be attached to the intent when the
	 *     alarm activity is started.
	 */
	private void showAlarmActivity(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		NacContext.startAlarm(this, alarm);
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
	 * Show a snackbar for the next alarm that will run.
	 */
	public void showNextAlarmSnackbar()
	{
		NacSharedConstants cons = this.getSharedConstants();
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarmCardAdapter cardAdapter = this.getAlarmCardAdapter();

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
	 * Update the notification.
	 *
	 * TODO: Check if race condition with this being called after submitList?
	 * Should I just pass a list of alarms to this method?
	 */
	public void updateUpcomingNotification()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarmCardAdapter cardAdapter = this.getAlarmCardAdapter();

		if (shared.getUpcomingAlarmNotification())
		{
			List<NacAlarm> alarms = cardAdapter.getCurrentList();
			NacUpcomingAlarmNotification notification =
				new NacUpcomingAlarmNotification(this);

			notification.setAlarmList(alarms);
			notification.show();
		}
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
	private boolean wasNfcScannedForAlarm(Intent intent)
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
