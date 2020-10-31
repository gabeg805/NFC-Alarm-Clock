package com.nfcalarmclock;

import android.content.Context;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Alarm card adapter.
 */
@SuppressWarnings("UnnecessaryInterfaceModifier")
public class NacCardAdapter
	extends RecyclerView.Adapter<NacCardHolder>
	implements View.OnClickListener,
		View.OnCreateContextMenuListener,
		MenuItem.OnMenuItemClickListener,
		RecyclerView.OnItemTouchListener,
		NacAlarm.OnAlarmChangeListener,
		NacCardHolder.OnCardCollapsedListener,
		NacCardHolder.OnCardExpandedListener,
		NacCardHolder.OnDeleteClickedListener,
		NacCardTouchHelper.Adapter
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
	private final CoordinatorLayout mRoot;

	/**
	 * RecyclerView containing list of alarm cards.
	 */
	private final RecyclerView mRecyclerView;

	/**
	 * Shared preferences.
	 */
	private final NacSharedPreferences mSharedPreferences;

	/**
	 * Handle card swipe events.
	 */
	private final NacCardTouchHelper mTouchHelper;

	/**
	 * The alarm to restore, when prompted after deletion.
	 */
	private final Undo mUndo;

	/**
	 * The snackbar.
	 */
	private final NacSnackbar mSnackbar;

	/**
	 * Upcoming notifications.
	 */
	private final NacUpcomingAlarmNotification mNotification;

	/**
	 * List of alarms.
	 */
	private List<NacAlarm> mAlarmList;

	/**
	 * Indicator that the alarm was added through the floating action button.
	 */
	private boolean mWasAddedWithFloatingActionButton;

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

		CoordinatorLayout root = activity.findViewById(R.id.activity_main);
		RecyclerView rv = root.findViewById(R.id.content_alarm_list);
		this.mRoot = root;
		this.mRecyclerView = rv;
		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mTouchHelper = new NacCardTouchHelper(callback);
		this.mUndo = new Undo();
		this.mNotification = new NacUpcomingAlarmNotification(context);
		this.mSnackbar = new NacSnackbar(root);
		this.mAlarmList = null;
		this.mWasAddedWithFloatingActionButton = false;
		this.mLastCardClicked = null;

		rv.addOnItemTouchListener(this);
		setHasStableIds(true);
	}

	/**
	 * Create a new alarm and add it to the list.
	 *
	 * @see #addAlarm(NacAlarm)
	 */
	public void addAlarm()
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

		this.addAlarm(alarm);
	}

	/**
	 * @see #addAlarm(NacAlarm, int)
	 *
	 * @param  alarm  The alarm to add.
	 */
	public void addAlarm(NacAlarm alarm)
	{
		int index = this.whereToInsertAlarm(alarm);
		this.addAlarm(alarm, index);
	}

	/**
	 * Add an alarm to the given index in the list.
	 *
	 * @param  alarm  The alarm to add.
	 * @param  index  The index to add the alarm.
	 */
	public void addAlarm(NacAlarm alarm, int index)
	{
		if (this.atMaxAlarmCapacity())
		{
			this.toastMaxAlarmsError();
			return;
		}

		Context context = this.getContext();

		NacTaskWorker.addAlarm(context, alarm);
		this.notifyInsertAlarm(alarm, index);
		this.updateNotification();
	}

	/**
	 * @return True if the maximum number of alarms has been created, and False
	 *     otherwise.
	 */
	public boolean atMaxAlarmCapacity()
	{
		NacSharedConstants cons = this.getSharedConstants();
		int size = this.size();

		return ((size+1) > cons.getMaxAlarms());
		//return ((size+1) <= cons.getMaxAlarms());
	}

	/**
	 * Build the alarm list.
	 */
	public void build()
	{
		Context context = this.getContext();
		NacSharedPreferences shared = this.getSharedPreferences();
		this.mAlarmList = NacDatabase.read(context);

		if (shared.getAppFirstRun())
		{
			for (NacAlarm a : this.getAlarms())
			{
				this.onAlarmChange(a);
			}

			shared.editAppFirstRun(false);
		}

		shared.editCardIsMeasured(false);
		this.getTouchHelper().setRecyclerView(this.getRecyclerView());
		this.getTouchHelper().reset();
		this.sort();
		this.updateNotification();
		notifyDataSetChanged();
	}

	/**
	 * Call the use NFC change listener.
	 */
	private void callOnUseNfcChangeListener(NacAlarm alarm)
	{
		OnUseNfcChangeListener listener = this.getOnUseNfcChangeListener();
		if (listener != null)
		{
			listener.onUseNfcChange(alarm);
			alarm.resetChangeTracker();
		}
	}

	/**
	 * @return True if the alarm can be inserted at the current state in the
	 *         alarm list, or False otherwise.
	 */
	private boolean canInsertAlarm(NacAlarm alarmToInsert, NacAlarm alarmInList,
		Calendar nextRun)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		boolean insertInUse = alarmToInsert.isInUse(shared);
		boolean listInUse = alarmInList.isInUse(shared);
		boolean insertEnabled = alarmToInsert.getEnabled();
		boolean listEnabled = alarmInList.getEnabled();

		if (insertInUse && !listInUse)
		{
			return true;
		}
		else if (insertInUse && listInUse)
		{
			Calendar cal = NacCalendar.getNextAlarmDay(alarmInList);
			return nextRun.before(cal);
		}

		if (insertEnabled && !listEnabled)
		{
			return true;
		}
		else if (insertEnabled == listEnabled)
		{
			Calendar cal = NacCalendar.getNextAlarmDay(alarmInList);
			return nextRun.before(cal);
		}

		return false;
	}

	/**
	 * Copy the alarm at the given index.
	 *
	 * @param  index  The index of the alarm to copy.
	 */
	public void copyAlarm(int index)
	{
		notifyItemChanged(index);

		if (this.atMaxAlarmCapacity())
		{
			this.toastMaxAlarmsError();
			return;
		}

		NacSharedConstants cons = this.getSharedConstants();
		NacAlarm alarm = this.getAlarm(index);
		NacAlarm alarmCopy = alarm.copy(this.getUniqueId());
		String message = cons.getMessageAlarmCopy();
		String action = cons.getActionUndo();

		this.addAlarm(alarmCopy, index+1);
		this.undo(alarmCopy, index+1, Undo.Type.COPY);
		this.showSnackbar(message, action, this);
	}

	/**
	 * Delete the alarm at the given index.
	 *
	 * @param  index  The index of the alarm to delete.
	 */
	public void deleteAlarm(int index)
	{
		Context context = this.getContext();
		NacSharedConstants cons = this.getSharedConstants();
		NacAlarm alarm = this.getAlarm(index);
		String message = cons.getMessageAlarmDelete();
		String action = cons.getActionUndo();

		NacTaskWorker.deleteAlarm(context, alarm);
		this.notifyDeleteAlarm(index);
		this.updateNotification();
		this.undo(alarm, index, Undo.Type.DELETE);
		this.showSnackbar(message, action, this);
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
		int size = this.size();
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
		NacSharedPreferences shared = this.getSharedPreferences();
		List<NacAlarm> inUseAlarms = new ArrayList<>();
		List<NacAlarm> enabledAlarms = new ArrayList<>();
		List<NacAlarm> disabledAlarms = new ArrayList<>();
		List<NacAlarm> sorted = new ArrayList<>();

		for (NacAlarm a : this.getAlarms())
		{
			List<NacAlarm> list;
			if (a.isInUse(shared))
			{
				list = inUseAlarms;
			}
			else if (a.getEnabled())
			{
				list = enabledAlarms;
			}
			else
			{
				list = disabledAlarms;
			}

			Calendar next = NacCalendar.getNextAlarmDay(a);
			int pos = 0;

			for (NacAlarm e : list)
			{
				Calendar cal = NacCalendar.getNextAlarmDay(e);

				if (next.before(cal))
				{
					break;
				}

				pos++;
			}

			list.add(pos, a);
		}

		sorted.addAll(inUseAlarms);
		sorted.addAll(enabledAlarms);
		sorted.addAll(disabledAlarms);
		return sorted;
	}

	/**
	 * @return The index where the sorted alarm should be inserted.
	 */
	private int getSortInsertIndex(NacAlarm alarm, int index)
	{
		if (alarm == null)
		{
			return -1;
		}

		int whereIndex = this.whereToInsertAlarm(alarm);
		return (whereIndex > index) ? whereIndex-1 : whereIndex;
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
	 * Highlight an alarm card.
	 */
	public void highlight(NacAlarm alarm)
	{
		NacCardHolder holder = this.getCardHolder(alarm);
		if (holder != null)
		{
			holder.highlight();
		}
	}

	/**
	 * @return True if is a valid sort index, and False otherwise.
	 */
	private boolean isValidSortIndex(int index, int insertIndex)
	{
		return (index >= 0) && (index != insertIndex);
	}

	/**
	 * Delete an alarm and notify any registered observers.
	 */
	public void notifyDeleteAlarm(int index)
	{
		if (index < 0)
		{
			return;
		}

		List<NacAlarm> alarmList = this.getAlarms();

		alarmList.remove(index);
		notifyItemRemoved(index);
	}

	/**
	 * Insert an alarm and notify any registered observers.
	 */
	public void notifyInsertAlarm(NacAlarm alarm, int index)
	{
		if ((alarm == null) || (index < 0))
		{
			return;
		}

		List<NacAlarm> alarmList = this.getAlarms();

		alarmList.add(index, alarm);
		notifyItemInserted(index);
	}

	/**
	 * Called when the alarm has been changed.
	 *
	 * @param  alarm  The alarm that was changed.
	 */
	@Override
	public void onAlarmChange(NacAlarm alarm)
	{
		Context context = this.getContext();

		if (alarm.wasChanged())
		{
			if (alarm.wasUseNfcChanged())
			{
				this.callOnUseNfcChangeListener(alarm);
			}
			else if (!alarm.isChangeTrackerLatched())
			{
				this.showAlarmChange(alarm);
				this.sortHighlight(alarm);
			}
		}

		NacTaskWorker.updateAlarm(context, alarm);
		this.updateNotification();
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
		card.setOnCardCollapsedListener(this);
		card.setOnCardExpandedListener(this);
		card.setOnDeleteClickedListener(this);
		card.setOnCreateContextMenuListener(this);

		if (this.wasAddedWithFloatingActionButton())
		{
			card.interact();
		}

		this.setWasAddedWithFloatingActionButton(false);
	}

	/**
	 * Called when the alarm card is collapsed.
	 */
	@Override
	public void onCardCollapsed(NacCardHolder holder, NacAlarm alarm)
	{
		if (alarm.wasChanged())
		{
			this.showAlarmChange(alarm);
			this.sortHighlight(alarm);
		}
	}

	/**
	 * Called when the alarm card is expanded.
	 */
	@Override
	public void onCardExpanded(NacCardHolder holder, NacAlarm alarm)
	{
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
	 * Create the view holder.
	 *
	 * @param  parent  The parent view.
	 * @param  viewType  The type of view.
	 */
	@NonNull
    @Override
	public NacCardHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		Context context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);
		View root = inflater.inflate(R.layout.card_frame, parent, false);

		return new NacCardHolder(root);
	}

	/**
	 * Delete button was clicked.
	 */
	@Override
	public void onDeleteClicked(int position)
	{
		this.deleteAlarm(position);
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
	 * Called when an alarm card was swiped to be copied.
	 *
	 * @param  index  The index of the alarm to copy.
	 */
	@Override
	public void onItemCopy(int index)
	{
		this.copyAlarm(index);
	}

	/**
	 * Called when an alarm card was swiped to be deleted.
	 * 
	 * @param  index  The index of the alarm to delete.
	 */
	@Override
	public void onItemDelete(int index)
	{
		this.deleteAlarm(index);
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
	 * Note: Needed for RecyclerView.OnItemTouchListener
	 */
	public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept)
	{
	}

	/**
	 * Note: Needed for RecyclerView.OnItemTouchListener
	 */
	public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e)
	{
	}

	/**
	 * Restore a previously deleted alarm.
	 * 
	 * @param  alarm  The alarm to restore.
	 * @param  position  The position to insert the alarm.
	 */
	public void restore(NacAlarm alarm, int position)
	{
		if (this.atMaxAlarmCapacity())
		{
			this.toastMaxAlarmsError();
			return;
		}

		NacSharedConstants cons = this.getSharedConstants();
		Locale locale = Locale.getDefault();
		String message = String.format(locale, "%1$s.",
			cons.getMessageAlarmRestore());
		String action = cons.getActionUndo();

		this.addAlarm(alarm, position);
		this.undo(alarm, position, Undo.Type.RESTORE);
		this.showSnackbar(message, action, this);
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
	 * @return True if the alarm should be sorted, and False otherwise.
	 */
	private boolean shouldSortAlarm(NacAlarm alarm)
	{
		NacCardHolder holder = this.getCardHolder(alarm);
		return (holder != null) && holder.isCollapsed() && alarm.wasChanged();
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
			this.showAlarm(alarm);
		}
		else
		{
			this.showNextAlarm();
		}
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

		Context context = this.getContext();
		NacSharedConstants cons = this.getSharedConstants();
		Locale locale = Locale.getDefault();
		String id = alarm.getNfcTagId();
		String message;
		
		if (!id.isEmpty())
		{
			message = String.format(locale, "%1$s %2$s",
				cons.getMessageShowNfcTagId(), id);
		}
		else
		{
			message = String.format(locale, "%1$s", cons.getMessageAnyNfcTagId());
		}

		NacUtility.quickToast(context, message);
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
	 * @return The number of elements in the adapter.
	 */
	public int size()
	{
		return this.getAlarms().size();
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
	public int sortAlarm(NacAlarm alarm)
	{
		if (!this.shouldSortAlarm(alarm))
		{
			return -1;
		}

		int findIndex = this.findAlarm(alarm);
		int insertIndex = this.getSortInsertIndex(alarm, findIndex);

		if (this.isValidSortIndex(findIndex, insertIndex))
		{
			this.notifyDeleteAlarm(findIndex);
			this.notifyInsertAlarm(alarm, insertIndex);
		}

		return insertIndex;
	}

	/**
	 * Sort and highlight the alarm, if the alarm card is collapsed.
	 */
	public void sortHighlight(NacAlarm alarm)
	{
		int sortIndex = this.sortAlarm(alarm);

		//if (alarm.isChangeTrackerLatched() && (findIndex == sortIndex))
		//{
		//}
		//else if (sortIndex >= 0)
		if (sortIndex >= 0)
		{
			this.getRecyclerView().scrollToPosition(sortIndex);
			this.highlight(alarm);
		}
	}

	/**
	 * Toast for when the maximum number of alarms has been created.
	 */
	public void toastMaxAlarmsError()
	{
		Context context = this.getContext();
		NacSharedConstants cons = this.getSharedConstants();

		NacUtility.quickToast(context, cons.getErrorMessageMaxAlarms());
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
	private int whereToInsertAlarm(NacAlarm alarm)
	{
		List<NacAlarm> alarmList = this.getAlarms();
		Calendar next = NacCalendar.getNextAlarmDay(alarm);
		int id = alarm.getId();
		int size = this.size();

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
