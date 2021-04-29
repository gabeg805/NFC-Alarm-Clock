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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
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
	private NacAlarmCardAdapter mCardAdapter;

	/**
	 * Scan an NFC tag dialog.
	 */
	private NacScanNfcTagDialog mScanNfcTagDialog;

	/**
	 * Alarm activity delay handler.
	 */
	private Handler mActivityDelayHandler;

	/**
	 * Shutdown broadcast receiver.
	 */
	private NacShutdownBroadcastReceiver mShutdownBroadcastReceiver;

	/**
	 * Alarm view model.
	 */
	private NacAlarmViewModel mAlarmViewModel;

	/**
	 * Alarm card touch helper.
	 */
	private NacCardTouchHelper mAlarmCardTouchHelper;

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
	 * Listener for when the floating action button is clicked.
	 */
	private final View.OnClickListener mFloatingActionButtonListener =
		new View.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				NacSharedPreferences shared = getSharedPreferences();
				NacAlarmCardAdapter cardAdapter = getCardAdapter();

				NacAlarm alarm = new NacAlarm.Builder(shared).build();
				// Test how this should work
				cardAdapter.setWasAddedWithFloatingActionButton(true);
				getAlarmViewModel().insert(NacMainActivity.this, alarm);
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
		NacAlarmCardAdapter cardAdapter = this.getCardAdapter();
		NacAlarm alarm = NacIntent.getSetAlarm(this, intent);

		if (alarm != null)
		{
			cardAdapter.setWasAddedWithFloatingActionButton(true);
			this.getAlarmViewModel().insert(this, alarm);
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
	 * Cleanup the show activity delay handler.
	 */
	private void cleanupShowActivityDelayHandler()
	{
		Handler handler = this.getShowActivityDelayHandler();
		if (handler != null)
		{
			handler.removeCallbacksAndMessages(null);
			this.mActivityDelayHandler = null;
		}
	}

	/**
	 * Cleanup the shutdown broadcast receiver.
	 */
	private void cleanupShutdownBroadcastReceiver()
	{
		NacShutdownBroadcastReceiver receiver = this.getShutdownBroadcastReceiver();
		unregisterReceiver(receiver);
	}

	/**
	 */
	public void copyAlarm(NacAlarm alarm)
	{
		NacSharedConstants cons = this.getSharedConstants();
		String message = cons.getMessageAlarmCopy();
		String action = cons.getActionUndo();

		try
		{
			Future<?> futureId = this.getAlarmViewModel().copy(alarm);
			long id = (Long) futureId.get();

			alarm.setId(id);
			NacUtility.printf("Id of copied row! %d", id);
		}
		//catch (ExecutionException e)
		catch (Exception e)
		{
			NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH exception!");
			NacUtility.printf("String  : %s!", e.toString());
			NacUtility.printf("Message : %s!", e.getMessage());
			e.printStackTrace();
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
	 * Setup an NFC scan checker, which checks if this activity was started by an
	 * NFC tag being scanned.
	 */
	private void dismissActiveAlarm(Intent intent)
	{
		NacAlarm alarm = NacDatabase.findActiveAlarm(this);
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
	 * @return The alarm view model.
	 */
	private NacAlarmViewModel getAlarmViewModel()
	{
		return this.mAlarmViewModel;
	}

	/**
	 * @return The alarm card adapter.
	 */
	private NacAlarmCardAdapter getCardAdapter()
	{
		return this.mCardAdapter;
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
	 * Show activity handler.
	 */
	private Handler getShowActivityDelayHandler()
	{
		return mActivityDelayHandler;
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
	 * Uncheck the NFC button when the dialog is canceled.
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	{
		NacAlarm alarm = (NacAlarm) dialog.getData();
		NacAlarmCardAdapter cardAdapter = this.getCardAdapter();
		NacCardHolder cardHolder = cardAdapter.getCardHolder(alarm);

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
		//if (alarm.wasChanged())
		//{
			NacUtility.printf("Show alarm collapse change!");
			//this.showAlarmChange(alarm);
			//this.sortHighlight(alarm);
			holder.highlight();
		//}
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
		//Context context = this.getContext();

		alarm.print();
		if (holder.isCollapsed())
		{
			NacUtility.printf("Show alarm updated!");
			//this.showAlarmChange(alarm);
			//this.sortHighlight(alarm);
			holder.highlight(); // I think this is only here in place of sortHighlight
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
		NacUtility.printf("CALLING ONCHANGED! %d", alarms.size());
		NacAlarmCardAdapter cardAdapter = this.getCardAdapter();

		//NacSharedPreferences shared = this.getSharedPreferences();
		//List<NacAlarm> inUseAlarms = new ArrayList<>();
		//List<NacAlarm> enabledAlarms = new ArrayList<>();
		//List<NacAlarm> disabledAlarms = new ArrayList<>();
		//List<NacAlarm> sorted = new ArrayList<>();

		//for (NacAlarm a : alarms)
		//{
		//	List<NacAlarm> list;
		//	if (a.isInUse(shared))
		//	{
		//		list = inUseAlarms;
		//	}
		//	else if (a.isEnabled())
		//	{
		//		list = enabledAlarms;
		//	}
		//	else
		//	{
		//		list = disabledAlarms;
		//	}

		//	Calendar next = NacCalendar.getNextAlarmDay(a);
		//	int pos = 0;

		//	for (NacAlarm e : list)
		//	{
		//		Calendar cal = NacCalendar.getNextAlarmDay(e);

		//		if (next.before(cal))
		//		{
		//			break;
		//		}

		//		pos++;
		//	}

		//	list.add(pos, a);
		//}

		//sorted.addAll(inUseAlarms);
		//sorted.addAll(enabledAlarms);
		//sorted.addAll(disabledAlarms);

		//cardAdapter.submitList(sorted);
		cardAdapter.submitList(alarms);
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
		NacAlarmCardAdapter adapter = this.getCardAdapter();
		RecyclerView rv = this.getRecyclerView();
		NacCardHolder holder = (NacCardHolder) rv.findViewHolderForAdapterPosition(index);
		//ViewHolder holder = rv.findViewHolderForAdapterPosition(index);

		//this.mAlarmCardTouchHelper.attachToRecyclerView(null);
		//this.mAlarmCardTouchHelper.attachToRecyclerView(this.mRecyclerView);
		this.mAlarmCardTouchHelper.getCallback().clearView(rv, holder);

		// Look into why "onButtonChecked" is printed when running this
		//adapter.notifyItemChanged(index);
		this.getLastAlarmCardAction().setIndex(adapter.getItemCount());
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
		this.mSharedPreferences = new NacSharedPreferences(this);
		this.mCardAdapter = new NacAlarmCardAdapter(this);
		this.mFloatingActionButton = findViewById(R.id.fab_add_alarm);
		this.mRecyclerView = findViewById(R.id.content_alarm_list);
		this.mScanNfcTagDialog = null;
		this.mShutdownBroadcastReceiver = new NacShutdownBroadcastReceiver();


		// Testing view model
		NacUtility.printf("Setting up the view model and everything!");
		this.mLastAlarmCardAction = new NacLastAlarmCardAction();
		this.mAlarmViewModel = new ViewModelProvider(this).get(NacAlarmViewModel.class);
		NacUtility.printf("GETTING ALL ALARMS AND OBSERVING!");
		this.getAlarmViewModel().getAllAlarms().observe(this, this);
		this.mAlarmCardTouchHelper = new NacCardTouchHelper(this);
		this.mAlarmCardTouchHelper.attachToRecyclerView(this.mRecyclerView);
		View root = findViewById(R.id.activity_main);
		this.mSnackbar = new NacSnackbar(root);
		this.mRecyclerView.addOnItemTouchListener(this);
		this.mRecyclerView.setHasFixedSize(true);
		this.mCardAdapter.setOnViewHolderCreatedListener(this);
		this.mCardAdapter.build();



		if (this.wasNfcScannedForAlarm(intent))
		{
			this.dismissActiveAlarm(intent);
		}

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
				this.showAlarm(alarm);
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
			NacAlarmCardAdapter cardAdapter = this.getCardAdapter();
			this.showNextAlarm();
			//cardAdapter.showNextAlarm();
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
		this.cleanupShowActivityDelayHandler();
		this.cleanupShutdownBroadcastReceiver();
		NacNfc.stop(this);

		NacUtility.printf("onPause!");
		this.ahhh();
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
		this.setupRefreshMainActivity();
		this.setupActiveAlarmActivity();
		this.setupAlarmCardAdapter();

		NacAlarmCardAdapter cardAdapter = this.getCardAdapter();
		List<NacAlarm> alarms = cardAdapter.getCurrentList();
		if (this.mSharedPreferences.getAppFirstRun() && (alarms.size() > 0))
		{
			NacScheduler.updateAll(this, alarms);
			this.mSharedPreferences.editAppFirstRun(false);
		}

		this.setupFloatingActionButton();
		this.setupGoogleRatingDialog();
		this.addSetAlarmFromIntent();
		this.setupShutdownBroadcastReceiver();
		NacNfc.start(this);
	}

	/**
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		NacUtility.printf("onDestroy!");
		this.ahhh();
	}

	/**
	 */
	private void ahhh()
	{

		File file = getDatabasePath(NacAlarmDatabase.DB_NAME);
		NacUtility.printf("Attempting to deleting the file : %s", file.getPath());

		if (file.exists())
		{
			NacUtility.printf("Deleting the database file!");
			file.delete();
		}
	}

	/**
	 * Note: Needed for RecyclerView.OnItemTouchListener
	 */
	public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e)
	{
	}

	/**
	 * Needed for NacAlarmCardAdapter.OnViewHolderCreatedListener.
	 */
	@Override
	public void onViewHolderCreated(NacCardHolder holder)
	{
		holder.setOnCardCollapsedListener(this);
		holder.setOnCardDeleteClickedListener(this);
		holder.setOnCardExpandedListener(this);
		holder.setOnCardMediaClickedListener(this);
		holder.setOnCardUpdatedListener(this);
		holder.setOnCardUseNfcChangedListener(this);
		holder.setOnCreateContextMenuListener(this);
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
	 * Setup the alarm activity for any active alarms.
	 */
	private void setupActiveAlarmActivity()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = NacDatabase.findActiveAlarm(this);
		long delay = SHOW_ALARM_ACTIVITY_DELAY;

		if (this.shouldShowAlarmActivity(alarm))
		{
			if (this.shouldShowAlarmActivityDelayed(alarm))
			{
				delay = shared.getPreventAppFromClosing() ? delay/4 : delay;
				this.showAlarmActivityDelayed(delay);
			}
			else
			{
				this.showAlarmActivity(alarm);
			}
		}
	}

	/**
	 * Setup the alarm card adapter.
	 */
	private void setupAlarmCardAdapter()
	{
		NacAlarmCardAdapter cardAdapter = this.getCardAdapter();

		//if (cardAdapter.size() == 0)
		//{
		//	cardAdapter.build();
		//}
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
	 * Setup the recycler view.
	 */
	private void setupRecyclerView()
	{
		RecyclerView recyclerView = this.getRecyclerView();
		NacAlarmCardAdapter cardAdapter = this.getCardAdapter();
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
		recyclerView.addItemDecoration(divider);
		recyclerView.setAdapter(cardAdapter);
		recyclerView.setLayoutManager(layoutManager);
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

		registerReceiver(receiver, filter);
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
	 * @return True if should delay the start of the alarm activity, and False
	 *     otherwise.
	 */
	private boolean shouldShowAlarmActivityDelayed(NacAlarm alarm)
	{
		return this.shouldShowAlarmActivity(alarm) && alarm.shouldUseNfc();
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

		Intent intent = NacIntent.createAlarmActivity(this, alarm);
		startActivity(intent);
	}

	/**
	 * Show the most recently edited alarm.
	 */
	public void showAlarm(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return;
		}

		NacSharedConstants cons = this.getSharedConstants();
		NacSharedPreferences shared = this.getSharedPreferences();
		String message = NacCalendar.getMessageWillRun(shared, alarm);
		String action = cons.getActionDismiss();

		this.showSnackbar(message, action);
	}

	/**
	 * Show the alarm activity after some delay.
	 */
	private void showAlarmActivityDelayed(long delay)
	{
		Handler handler = new Handler();

		handler.postDelayed(new Runnable() {
			@Override
			public void run()
			{
				NacAlarm activeAlarm = NacDatabase.findActiveAlarm(NacMainActivity.this);
				if (shouldShowAlarmActivity(activeAlarm))
				{
					showAlarmActivity(activeAlarm);
				}
			}
		}, delay);

		this.mActivityDelayHandler = handler;
	}

	///**
	// * Show a message either for the recently changed alarm, or the next alarm.
	// */
	//private void showAlarmChange(NacAlarm alarm)
	//{
	//	if (!alarm.wasChanged())
	//	{
	//		return;
	//	}

	//	if (alarm.isEnabled())
	//	{
	//		this.showAlarm(alarm);
	//	}
	//	else
	//	{
	//		this.showNextAlarm();
	//	}
	//}

	/**
	 * Show the next alarm.
	 */
	public void showNextAlarm()
	{
		NacSharedConstants cons = this.getSharedConstants();
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarmCardAdapter cardAdapter = this.getCardAdapter();

		List<NacAlarm> alarms = cardAdapter.getCurrentList();
		NacAlarm alarm = NacCalendar.getNextAlarm(alarms);
		//NacAlarm alarm = this.getNextAlarm();
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
	 * Update the notification.
	 *
	 * TODO: Check if race condition with this being called after submitList?
	 * Should I just pass a list of alarms to this method?
	 */
	public void updateUpcomingNotification()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarmCardAdapter cardAdapter = this.getCardAdapter();

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
