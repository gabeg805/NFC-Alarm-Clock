package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Alarm card adapter.
 */
public class NacCardAdapter
	extends RecyclerView.Adapter<NacCardHolder>
	implements View.OnClickListener,
		View.OnCreateContextMenuListener,
		MenuItem.OnMenuItemClickListener,
		RecyclerView.OnItemTouchListener,
		NacAlarm.OnAlarmChangeListener,
		NacCardDelete.OnDeleteListener,
		NacCardTouchHelper.Adapter,
		NacCardView.OnStateChangeListener
{

	/**
	 * Definition for the use NFC change listener.
	 */
	public interface OnUseNfcChangeListener
	{
		public void onUseNfcChange(NacAlarm alarm);
	}

	/**
	 * Listener for when use NFC is changed.
	 */
	private OnUseNfcChangeListener mOnUseNfcChangeListener;

	/**
	 * Main activity root view.
	 */
	private CoordinatorLayout mRoot;

	/**
	 * RecyclerView containing list of alarm cards.
	 */
	private RecyclerView mRecyclerView;

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Handle card swipe events.
	 */
	private NacCardTouchHelper mTouchHelper;

	/**
	 * The alarm to restore, when prompted after deletion.
	 */
	private Undo mUndo;

	/**
	 * The snackbar.
	 */
	private NacSnackbar mSnackbar;

	/**
	 * Upcoming notifications.
	 */
	private NacUpcomingAlarmNotification mNotification;

	/**
	 * List of alarms.
	 */
	private List<NacAlarm> mAlarmList;

	/**
	 * Previous alarm to go off, in calendar form.
	 */
	private Calendar mPreviousCalendar;

	/**
	 * Indicator that the alarm was added through the floating action button.
	 */
	private boolean mWasAddedWithFloatingActionButton;

	/**
	 * Alarm card measure.
	 */
	private NacCardMeasure mMeasure;

	/**
	 * Card that was last clicked on to show a menu.
	 */
	private View mLastCardClicked;

	/**
	 */
	public NacCardAdapter(Context context)
	{
		AppCompatActivity activity = (AppCompatActivity) context;
		NacCardTouchHelper.Callback callback =
			new NacCardTouchHelper.Callback(this);

		this.mRoot = (CoordinatorLayout) activity.findViewById(
			R.id.activity_main);
		this.mRecyclerView = (RecyclerView) this.getRoot().findViewById(
			R.id.content_alarm_list);
		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mTouchHelper = new NacCardTouchHelper(callback);
		this.mUndo = new Undo();
		this.mNotification = new NacUpcomingAlarmNotification(context);
		this.mSnackbar = new NacSnackbar(this.mRoot);
		this.mAlarmList = null;
		this.mPreviousCalendar = null;
		this.mWasAddedWithFloatingActionButton = false;
		this.mMeasure = new NacCardMeasure(context);
		this.mLastCardClicked = null;

		this.mRecyclerView.addOnItemTouchListener(this);
		setHasStableIds(true);
	}

	/**
	 * Add an alarm.
	 */
	public int addAlarm()
	{
		Context context = this.getContext();
		NacSharedPreferences shared = this.getSharedPreferences();
		int id = this.getUniqueId();
		NacAlarm alarm = new NacAlarm.Builder(context)
			.setId(id)
			.setRepeat(shared.getRepeat())
			.setDays(shared.getDays())
			.setUseNfc(shared.getUseNfc())
			.setVibrate(shared.getVibrate())
			.setVolume(shared.getVolume())
			.setMedia(context, shared.getMediaPath())
			.setName(shared.getName())
			.build();

		return this.addAlarm(alarm);
	}

	/**
	 * @see add
	 *
	 * @param  alarm  The alarm to add.
	 */
	public int addAlarm(NacAlarm alarm)
	{
		int index = this.whereToPutAlarm(alarm);
		return this.addAlarm(alarm, index);
	}

	/**
	 * @see add
	 *
	 * @param  alarm  The alarm to add.
	 * @param  position  The position to add the alarm.
	 */
	public int addAlarm(NacAlarm alarm, int position)
	{
		Context context = this.getContext();
		NacSharedConstants cons = this.getSharedConstants();

		if ((position+1) >= cons.getMaxAlarms())
		{
			NacUtility.quickToast(context, cons.getErrorMessageMaxAlarms());
			return -1;
		}

		NacDatabase db = new NacDatabase(context);
		int id = alarm.getId();

		NacScheduler.update(context, alarm);
		this.getSharedPreferences().editSnoozeCount(id, 0);
		this.getAlarms().add(position, alarm);
		this.updateNotification();
		notifyItemInserted(position);
		db.add(alarm);
		db.close();
		return 0;
	}

	/**
	 * Build the alarm list.
	 */
	public void build()
	{
		Context context = this.getContext();
		NacSharedPreferences shared = this.getSharedPreferences();
		NacDatabase db = new NacDatabase(context);
		this.mAlarmList = db.read();

		if (shared.getAppFirstRun())
		{
			for (NacAlarm a : this.getAlarms())
			{
				this.onAlarmChange(a);
			}

			shared.editAppFirstRun(false);
		}

		this.getTouchHelper().setRecyclerView(this.getRecyclerView());
		this.getTouchHelper().reset();
		this.sort();
		this.updateNotification();
		notifyDataSetChanged();
		db.close();
		this.mMeasure.measure(this.mRecyclerView);
	}

	/**
	 */
	private boolean canInsertAlarm(NacAlarm alarmToInsert, NacAlarm alarmInList,
		Calendar nextRun)
	{
		boolean insertEnabled = alarmToInsert.getEnabled();
		boolean alarmEnabled = alarmInList.getEnabled();

		if (insertEnabled && !alarmEnabled)
		{
			return true;
		}
		else if (insertEnabled == alarmEnabled)
		{
			Calendar cal = NacCalendar.getNext(alarmInList);

			if (nextRun.before(cal))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Copy the alarm.
	 *
	 * @param  pos	The position of the alarm card to copy.
	 */
	public int copyAlarm(int position)
	{
		notifyItemChanged(position);

		NacAlarm alarm = this.getAlarm(position);
		NacAlarm copy = alarm.copy(this.getUniqueId());
		int result = this.addAlarm(copy, position+1);

		if (result == 0)
		{
			NacSharedConstants cons = this.getSharedConstants();
			Locale locale = Locale.getDefault();
			String message = String.format(locale, "%1$s.",
				cons.getMessageAlarmCopy());

			this.undo(copy, position+1, Undo.Type.COPY);
			this.snackbar(message);
		}

		this.setWasAddedWithFloatingActionButton(false);

		return result;
	}

	/**
	 * Delete the alarm at the given position.
	 *
	 * @param  pos	The position of the alarm card to delete.
	 */
	public int deleteAlarm(int position)
	{
		Context context = this.getContext();
		NacSharedConstants cons = this.getSharedConstants();
		NacDatabase db = new NacDatabase(context);
		NacAlarm alarm = this.getAlarm(position);
		int id = alarm.getId();
		Locale locale = Locale.getDefault();
		String message = String.format(locale, "%1$s.",
			cons.getMessageAlarmDelete());

		NacScheduler.cancel(context, alarm);
		this.setWasAddedWithFloatingActionButton(false);
		this.getSharedPreferences().editSnoozeCount(id, 0);
		this.getAlarms().remove(position);
		this.updateNotification();
		notifyItemRemoved(position);
		db.delete(alarm);
		db.close();
		this.undo(alarm, position, Undo.Type.DELETE);
		this.snackbar(message);

		return 0;
	}

	/**
	 * @return The index at which the given alarm is found.
	 */
	public int findAlarm(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		List<NacAlarm> alarmList = this.getAlarms();
		int size = alarmList.size();
		int id = alarm.getId();

		for (int i=0; i < size; i++)
		{
			NacAlarm a = alarmList.get(i);

			if (a.getId() == id)
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * @return The alarm at the given index.
	 */
	public NacAlarm getAlarm(int index)
	{
		return this.getAlarms().get(index);
	}

	/**
	 * @return The list of alarms.
	 */
	public List<NacAlarm> getAlarms()
	{
		return this.mAlarmList;
	}

	/**
	 * @return The NacCardHolder for a given alarm.
	 */
	public NacCardHolder getCardHolder(NacAlarm alarm)
	{
		RecyclerView rv = this.getRecyclerView();
		if ((alarm == null) || (rv == null))
		{
			return null;
		}

		int id = alarm.getId();
		if (id <= 0)
		{
			return null;
		}

		return (NacCardHolder) rv.findViewHolderForItemId(id);
	}

	/**
	 * @return The NacCardView for a given alarm.
	 */
	public NacCardView getCardView(NacAlarm alarm)
	{
		NacCardHolder holder = this.getCardHolder(alarm);
		return (holder != null) ? holder.getNacCardView() : null;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.getRoot().getContext();
	}

	/**
	 * @return The unique ID of an alarm. Used alongside setHasStableIds().
	 */
	@Override
	public long getItemId(int position)
	{
		return this.getAlarm(position).getId();
	}

	/**
	 * @return The number of items in the recycler view.
	 */
	@Override
	public int getItemCount()
	{
		return this.size();
	}

	/**
	 * @return The next alarm that will be triggered.
	 */
	public NacAlarm getNextAlarm()
	{
		List<NacAlarm> alarms = this.getAlarms();

		return NacCalendar.getNextAlarm(alarms);
	}

	/**
	 * @return The notification.
	 */
	public NacUpcomingAlarmNotification getNotification()
	{
		return this.mNotification;
	}

	/**
	 * @return The on use NFC change listener.
	 */
	protected OnUseNfcChangeListener getOnUseNfcChangeListener()
	{
		return this.mOnUseNfcChangeListener;
	}

	/**
	 * @return The previous calendar.
	 */
	private Calendar getPreviousCalendar()
	{
		return this.mPreviousCalendar;
	}

	/**
	 * @return The RecyclerView.
	 */
	private RecyclerView getRecyclerView()
	{
		return this.mRecyclerView;
	}

	/**
	 * @return The root view.
	 */
	private View getRoot()
	{
		return this.mRoot;
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
	 * @return The snackbar.
	 */
	private NacSnackbar getSnackbar()
	{
		return this.mSnackbar;
	}

	/**
	 * @return The list of alarms, in sort order, from soonest to latest, with
	 *         disabled alarms at the end.
	 */
	public List<NacAlarm> getSortedAlarms()
	{
		List<NacAlarm> enabledAlarms = new ArrayList<>();
		List<NacAlarm> disabledAlarms = new ArrayList<>();
		List<NacAlarm> sorted = new ArrayList<>();

		for (NacAlarm a : this.getAlarms())
		{
			List<NacAlarm> list = (a.getEnabled()) ? enabledAlarms
				: disabledAlarms;
			Calendar next = NacCalendar.getNext(a);
			int pos = 0;

			for (NacAlarm e : list)
			{
				Calendar cal = NacCalendar.getNext(e);

				if (next.before(cal))
				{
					break;
				}

				pos++;
			}

			list.add(pos, a);
		}

		sorted.addAll(enabledAlarms);
		sorted.addAll(disabledAlarms);

		return sorted;
	}

	/**
	 * @return The touch helper.
	 */
	private NacCardTouchHelper getTouchHelper()
	{
		return this.mTouchHelper;
	}

	/**
	 * @return The undo object.
	 */
	private Undo getUndo()
	{
		return this.mUndo;
	}

	/**
	 * Determine a unique integer ID number to use for newly created alarms.
	 */
	public int getUniqueId()
	{
		List<Integer> used = new ArrayList<>();

		for (NacAlarm a : this.getAlarms())
		{
			used.add(a.getId());
		}

		for (int i=1; i < Integer.MAX_VALUE; i+=7)
		{
			if (!used.contains(i))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * Check if there is a use NFC change listener.
	 */
	public boolean hasUseNfcChangeListener()
	{
		return this.getOnUseNfcChangeListener() != null;
	}

	/**
	 * Highlight an alarm card.
	 */
	public void highlight(NacAlarm alarm)
	{
		NacCardView card = this.getCardView(alarm);
		if (card != null)
		{
			card.highlight();
		}
	}

	/**
	 * Insert the alarm at the given location.
	 */
	public int insertAlarm(NacAlarm alarm, int index)
	{
		if (index >= 0)
		{
			List<NacAlarm> alarmList = this.getAlarms();

			alarmList.add(index, alarm);
			notifyItemInserted(index);
			return index;
		}

		return -1;
	}

	/**
	 * Setup the alarm card.
	 *
	 * @param  card  The alarm card.
	 * @param  position  The position of the alarm card.
	 */
	@Override
	public void onBindViewHolder(final NacCardHolder card, int position)
	{
		NacAlarm alarm = this.getAlarm(position);

		alarm.setOnAlarmChangeListener(this);
		card.init(alarm);
		card.setOnDeleteListener(this);
		card.setOnCreateContextMenuListener(this);
		card.setOnStateChangeListener(this);

		if (this.wasAddedWithFloatingActionButton())
		{
			card.interact();
		}

		this.setWasAddedWithFloatingActionButton(false);
	}

	/**
	 * Create the context menu.
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
		ContextMenuInfo menuInfo)
	{
		if (menu.size() > 0)
		{
			return;
		}

		Context context = this.getContext();
		AppCompatActivity activity = (AppCompatActivity) context;
		this.mLastCardClicked = view;

		activity.getMenuInflater().inflate(R.menu.menu_card, menu);

		for (int i=0; i < menu.size(); i++)
		{
			MenuItem item = menu.getItem(i);

			item.setOnMenuItemClickListener(this);
		}
	}

	/**
	 * Catch when a menu item is clicked.
	 */
	@Override
	public boolean onMenuItemClick(MenuItem item)
	{
		RecyclerView rv = this.getRecyclerView();
		View view = this.mLastCardClicked;
		NacCardHolder holder = (NacCardHolder)rv.findContainingViewHolder(view);
		int id = item.getItemId();

		if (holder != null)
		{
			NacAlarm alarm = holder.getAlarm();
			int position = holder.getAdapterPosition();

			switch (id)
			{
				case R.id.menu_copy_alarm:
					this.copyAlarm(position);
					break;
				case R.id.menu_delete_alarm:
					this.deleteAlarm(position);
					break;
				case R.id.menu_show_next_alarm:
					this.showAlarm(alarm);
					break;
				case R.id.menu_show_nfc_tag_id:
					this.showNfcTagId(alarm);
					break;
				default:
					break;
			}
		}

		this.mLastCardClicked = null;

		return true;
	}

	/**
	 * Update the database when alarm data is changed.
	 *
	 * @param  a  The alarm that was changed.
	 */
	@Override
	public void onAlarmChange(NacAlarm alarm)
	{
		Context context = this.getContext();
		NacDatabase db = new NacDatabase(context);

		if (alarm.wasChanged())
		{
			if (alarm.wasUseNfcChanged() && this.hasUseNfcChangeListener())
			{
				this.getOnUseNfcChangeListener().onUseNfcChange(alarm);
				alarm.resetChangeTracker();
			}
			else if (!alarm.isChangeTrackerLatched())
			{
				this.showAlarmChange(alarm);
				this.sortHighlight(alarm);
			}
		}

		NacScheduler.update(context, alarm);
		this.setWasAddedWithFloatingActionButton(false);
		this.updateNotification();
		db.update(alarm);
		db.close();
	}

	/**
	 * Capture the click event on the delete button, and delete the card it
	 * belongs to.
	 *
	 * @param  view  The view that was clicked.
	 */
	@Override
	public void onClick(View view)
	{
		Undo undo = this.getUndo();
		NacAlarm alarm = undo.getAlarm();
		int position = undo.getPosition();
		Undo.Type type = undo.getType();

		undo.reset();

		if (type == Undo.Type.COPY)
		{
			this.deleteAlarm(position);
		}
		else if (type == Undo.Type.DELETE)
		{
			this.restore(alarm, position);
		}
		else if (type == Undo.Type.RESTORE)
		{
			this.deleteAlarm(position);
		}
	}

	/**
	 * Create the view holder.
	 *
	 * @param  parent  The parent view.
	 * @param  viewType  The type of view.
	 */
	@Override
	public NacCardHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		Context context = parent.getContext();
		View root = LayoutInflater.from(context).inflate(R.layout.card_frame,
			parent, false);
		NacCardHolder holder = new NacCardHolder(root, this.mMeasure);

		if (!this.mMeasure.isMeasured())
		{
			this.mMeasure.measure(holder);
		}

		return holder;
	}

	/**
	 * Delete the alarm.
	 */
	@Override
	public void onDelete(int position)
	{
		this.deleteAlarm(position);
	}

	/**
	 * @note Needed for RecyclerView.OnItemTouchListener
	 */
	public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent ev)
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
	 * Copy the alarm.
	 *
	 * @param  pos	The position of the alarm to copy.
	 */
	@Override
	public void onItemCopy(int position)
	{
		this.copyAlarm(position);
	}

	/**
	 * Delete the alarm.
	 * 
	 * @param  pos	The position of the alarm to delete.
	 */
	@Override
	public void onItemDelete(int position)
	{
		this.deleteAlarm(position);
	}

	/**
	 * @note Needed for RecyclerView.OnItemTouchListener
	 */
	public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept)
	{
	}

	/**
	 */
	public void onStateChange(NacCardView card, NacCardView.State state)
	{
		NacAlarm alarm = card.getAlarm();

		if (alarm.isChangeTrackerLatched() && card.isCollapsedState())
		{
			this.showAlarmChange(alarm);
			this.sortHighlight(alarm);
			alarm.unlatchChangeTracker();
		}
	}

	/**
	 * @note Needed for RecyclerView.OnItemTouchListener
	 */
	public void onTouchEvent(RecyclerView rv, MotionEvent e)
	{
	}

	/**
	 * Remove the alarm at the given index.
	 */
	public int removeAlarm(int index)
	{
		if (index >= 0)
		{
			List<NacAlarm> alarmList = this.getAlarms();

			alarmList.remove(index);
			notifyItemRemoved(index);
		}

		return -1;
	}

	/**
	 * Replace the alarm at the given index.
	 */
	public int replaceAlarm(NacAlarm alarm)
	{
		int index = this.findAlarm(alarm);
		if (index < 0)
		{
			return -1;
		}

		List<NacAlarm> alarmList = this.getAlarms();
		alarmList.set(index, alarm);
		notifyItemChanged(index);
		return index;
	}

	/**
	 * Restore a previously deleted alarm.
	 * 
	 * @param  alarm  The alarm to restore.
	 * @param  position  The position to insert the alarm.
	 */
	public void restore(NacAlarm alarm, int position)
	{
		int result = this.addAlarm(alarm, position);

		if (result == 0)
		{
			NacSharedConstants cons = this.getSharedConstants();
			Locale locale = Locale.getDefault();
			String message = String.format(locale, "%1$s.",
				cons.getMessageAlarmRestore());

			this.undo(alarm, position, Undo.Type.RESTORE);
			this.snackbar(message);
		}

		this.setWasAddedWithFloatingActionButton(false);
	}

	/**
	 * Save alarms to the database.
	 */
	public void saveAlarms()
	{
		Context context = this.getContext();
		NacDatabase db = new NacDatabase(context);
		List<NacAlarm> alarms = this.getAlarms();

		db.update(alarms);
		db.close();
	}

	/**
	 * Set the on use NFC change listener.
	 */
	public void setOnUseNfcChangeListener(OnUseNfcChangeListener listener)
	{
		this.mOnUseNfcChangeListener = listener;
	}

	/**
	 * Set whether an alarm was added with the floating button.
	 */
	public void setWasAddedWithFloatingActionButton(boolean added)
	{
		this.mWasAddedWithFloatingActionButton = added;
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

		this.snackbar(message, cons.getActionDismiss(), null, true);
	}

	/**
	 * Show a message either for the recently changed alarm, or the next alarm.
	 */
	private void showAlarmChange(NacAlarm alarm)
	{
		if (!alarm.wasChanged())
		{
			return;
		}

		if (alarm.getEnabled())
		{
			this.showAlarmRuntime(alarm);
		}
		else
		{
			this.showNextAlarm();
		}
	}

	/**
	 * Show when the alarm will next run.
	 */
	private void showAlarmRuntime(NacAlarm alarm)
	{
		Calendar alarmCalendar = NacCalendar.getNext(alarm);
		this.showAlarm(alarm);
		this.mPreviousCalendar = alarmCalendar;
	}

	/**
	 * Show the next alarm.
	 */
	public void showNextAlarm()
	{
		NacSharedConstants cons = this.getSharedConstants();
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getNextAlarm();
		String message = NacCalendar.getMessageNextAlarm(shared, alarm);

		if (alarm == null)
		{
			this.mPreviousCalendar = null;
		}

		this.snackbar(message, cons.getActionDismiss(), null, true);
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

		Context context = this.getContext();
		NacSharedConstants cons = this.getSharedConstants();
		Locale locale = Locale.getDefault();
		String id = alarm.getNfcTagId();
		String message = String.format(locale, "%1$s %2$s",
			cons.getMessageShowNfcTagId(), id);

		NacUtility.quickToast(context, message);
	}

	/**
	 * @return The number of elements in the adapter.
	 */
	public int size()
	{
		return this.getAlarms().size();
	}

	/**
	 * @see snackbar
	 */
	private void snackbar(String message)
	{
		NacSharedConstants cons = this.getSharedConstants();
		this.snackbar(message, cons.getActionUndo(), this, true);
	}

	/**
	 * Create a snackbar message.
	 */
	private void snackbar(String message, String action,
		View.OnClickListener listener, boolean dismiss)
	{
		NacSnackbar snackbar = this.getSnackbar();

		snackbar.show(message, action, listener, dismiss);
	}

	/**
	 * Sort the enabled alarms from soonest to latest.
	 */
	public void sort()
	{
		this.mAlarmList = this.getSortedAlarms();

		notifyDataSetChanged();
	}

	/**
	 * Sort an alarm into the alarm list.
	 */
	public int sortAlarm(NacAlarm alarm, int index)
	{
		if (alarm == null)
		{
			return -1;
		}

		int whereIndex = this.whereToPutAlarm(alarm);
		int insertIndex = whereIndex;

		if (index < 0)
		{
			insertIndex = -1;
		}
		else if (index != whereIndex)
		{
			insertIndex = (whereIndex > index) ? whereIndex-1 : whereIndex;

			this.removeAlarm(index);
			this.insertAlarm(alarm, insertIndex);
		}

		return insertIndex;
	}

	/**
	 * Sort an alarm into the alarm list.
	 */
	public int sortAlarm(NacAlarm alarm)
	{
		int index = this.findAlarm(alarm);
		return this.sortAlarm(alarm, index);
	}

	/**
	 * Sort and highlight the alarm, if the alarm card is collapsed.
	 */
	public void sortHighlight(NacAlarm alarm)
	{
		NacCardView card = this.getCardView(alarm);

		if (card == null)
		{
			return;
		}
		else if (card.isExpandedState())
		{
			alarm.latchChangeTracker();
		}
		else
		{
			int findIndex = this.findAlarm(alarm);
			int sortIndex = this.sortAlarm(alarm, findIndex);

			if (alarm.isChangeTrackerLatched() && (findIndex == sortIndex))
			{
			}
			else if (sortIndex >= 0)
			{
				this.getRecyclerView().scrollToPosition(sortIndex);
				this.highlight(alarm);
			}
		}
	}

	/**
	 * Insert the alarm into the sorted list.
	 */
	private int sortInsertAlarm(NacAlarm alarm)
	{
		int index = this.whereToPutAlarm(alarm);

		return this.insertAlarm(alarm, index);
	}

	/**
	 * @see sortRemoveAlarm
	 */
	private int sortRemoveAlarm(NacAlarm alarm)
	{
		int index = this.findAlarm(alarm);

		return this.removeAlarm(index);
	}

	/**
	 * Save undo parameters.
	 */
	public void undo(NacAlarm alarm, int position, Undo.Type type)
	{
		this.getUndo().set(alarm, position, type);
	}

	/**
	 * Update the notification.
	 */
	public void updateNotification()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		List<NacAlarm> alarms = this.getAlarms();

		if (shared.getUpcomingAlarmNotification())
		{
			NacUpcomingAlarmNotification notification = this.getNotification();
			notification.setAlarmList(alarms);
			notification.show();
		}
	}

	/**
	 * @return True if the alarm was added, and False otherwise.
	 */
	public boolean wasAddedWithFloatingActionButton()
	{
		return this.mWasAddedWithFloatingActionButton;
	}

	/**
	 * @return Where to insert the alarm in the list (ignoring it's current
	 *         position if it already exists in the list.
	 */
	private int whereToPutAlarm(NacAlarm alarm)
	{
		List<NacAlarm> alarmList = this.getAlarms();
		Calendar next = NacCalendar.getNext(alarm);
		int id = alarm.getId();
		int size = alarmList.size();

		for (int index=0; index < size; index++)
		{
			NacAlarm a = alarmList.get(index);

			if (a.getId() == id)
			{
				continue;
			}

			if (this.canInsertAlarm(alarm, a, next))
			{
				return index;
			}
		}

		return size;
	}

	/**
	 * Undo an alarm card.
	 */
	public static class Undo
	{

		/**
		 * Type of undo operation.
		 */
		public enum Type
		{
			NONE,
			COPY,
			DELETE,
			RESTORE
		}

		/**
		 * Alarm.
		 */
		public NacAlarm mAlarm;

		/**
		 * Position of the alarm card in the RecyclerView.
		 */
		public int mPosition;

		/**
		 * Type of undo.
		 */
		public Type mType;

		/**
		 */
		public Undo()
		{
			this.reset();
		}

		/**
		 * Reset the member variables.
		 */
		public void reset()
		{
			this.set(null, -1, Type.NONE);
		}

		/**
		 * Set the member variables.
		 *
		 * @param  alarm  The alarm info.
		 * @param  position  Position of the alarm card.
		 * @param  type  Type of undo.
		 */
		public void set(NacAlarm alarm, int position, Type type)
		{
			this.mAlarm = alarm;
			this.mPosition = position;
			this.mType = type;
		}

		/**
		 * @return The alarm.
		 */
		public NacAlarm getAlarm()
		{
			return this.mAlarm;
		}

		/**
		 * @return The position.
		 */
		public int getPosition()
		{
			return this.mPosition;
		}

		/**
		 * @return The undo type.
		 */
		public Type getType()
		{
			return this.mType;
		}

	}

}
