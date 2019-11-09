package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
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

/**
 * Alarm card adapter.
 */
public class NacCardAdapter
	extends RecyclerView.Adapter<NacCardHolder>
	implements View.OnClickListener,
		RecyclerView.OnItemTouchListener,
		NacAlarm.OnChangeListener,
		NacCardDelete.OnDeleteListener,
		NacCardTouchHelper.Adapter
{

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
	private NacSharedPreferences mShared;

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
	 * Next alarm to go off.
	 */
	private NacAlarm mNextAlarm;

	/**
	 * Previous alarm to go off, in calendar form.
	 */
	private Calendar mPreviousCalendar;

	/**
	 * Indicator that the alarm was added through the floating action button.
	 */
	private boolean mWasAddedWithFloatingButton;

	/**
	 * Alarm card measure.
	 */
	private NacCardMeasure mMeasure;

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
		this.mShared = new NacSharedPreferences(context);
		this.mTouchHelper = new NacCardTouchHelper(callback);
		this.mUndo = new Undo();
		this.mNotification = new NacUpcomingAlarmNotification(context);
		this.mSnackbar = new NacSnackbar(this.mRoot);
		this.mAlarmList = null;
		this.mNextAlarm = null;
		this.mPreviousCalendar = null;
		this.mWasAddedWithFloatingButton = false;
		this.mMeasure = new NacCardMeasure(context);

		this.mRecyclerView.addOnItemTouchListener(this);
		setHasStableIds(true);
	}

	/**
	 * Add an alarm.
	 */
	public int add()
	{
		Context context = this.getContext();
		NacSharedPreferences shared = this.getSharedPreferences();
		int id = this.getUniqueId();
		NacAlarm alarm = new NacAlarm.Builder()
			.setId(id)
			.setRepeat(shared.getRepeat())
			.setDays(shared.getDays())
			.setUseNfc(shared.getUseNfc())
			.setVibrate(shared.getVibrate())
			.setSound(context, shared.getSound())
			.setName(shared.getName())
			.build();

		return this.add(alarm);
	}

	/**
	 * @see add
	 *
	 * @param  alarm  The alarm to add.
	 */
	public int add(NacAlarm alarm)
	{
		int index = this.size();
		int result = this.add(alarm, index);

		if (result == 0)
		{
			this.getRecyclerView().scrollToPosition(index);
		}

		return result;
	}

	/**
	 * @see add
	 *
	 * @param  alarm  The alarm to add.
	 * @param  position  The position to add the alarm.
	 */
	public int add(NacAlarm alarm, int position)
	{
		if ((position+1) >= NacSharedPreferences.DEFAULT_MAX_ALARMS)
		{
			NacUtility.quickToast(this.getRoot(),
				"Max number of alarms created");
			return -1;
		}

		Context context = this.getContext();
		Intent intent = NacIntent.createService(context, "add", alarm);

		this.startService(intent);
		this.getAlarms().add(position, alarm);
		this.updateNotification();
		notifyItemInserted(position);

		return 0;
	}

	/**
	 * @return True if all alarms are disabled, and False otherwise.
	 */
	private boolean areAllAlarmsDisabled()
	{
		List<NacAlarm> alarms = this.getAlarms();

		for (NacAlarm a : alarms)
		{
			if (a.getEnabled())
			{
				return false;
			}
		}

		return true;
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
		this.mNextAlarm = NacCalendar.getNextAlarm(this.mAlarmList);

		if (shared.getAppFirstRun())
		{
			for (NacAlarm a : this.getAlarms())
			{
				this.onChange(a);
			}

			shared.editAppFirstRun(false);
		}

		this.getTouchHelper().setRecyclerView(this.getRecyclerView());
		this.getTouchHelper().reset();
		this.updateNotification();
		notifyDataSetChanged();
		db.close();
		this.mMeasure.measure(this.mRecyclerView);
	}

	/**
	 * Copy the alarm.
	 *
	 * @param  pos	The position of the alarm card to copy.
	 */
	public int copy(int position)
	{
		notifyItemChanged(position);

		NacAlarm alarm = this.get(position);
		NacAlarm copy = alarm.copy(this.getUniqueId());
		int result = this.add(copy);

		if (result == 0)
		{
			this.undo(copy, this.size()-1, Undo.Type.COPY);
			this.snackbar("Copied alarm.");
		}

		this.setWasAddedWithFloatingButton(false);

		return result;
	}

	/**
	 * Delete the alarm at the given position.
	 *
	 * @param  pos	The position of the alarm card to delete.
	 */
	public int delete(int position)
	{
		NacAlarm alarm = this.get(position);
		Context context = this.getContext();
		Intent intent = NacIntent.createService(context, "delete", alarm);

		this.setWasAddedWithFloatingButton(false);
		this.getAlarms().remove(position);
		this.updateNotification();
		notifyItemRemoved(position);
		this.undo(alarm, position, Undo.Type.DELETE);
		this.snackbar("Deleted alarm.");
		this.startService(intent);

		return 0;
	}

	/**
	 * @return The alarm at the given index.
	 */
	public NacAlarm get(int index)
	{
		return this.getAlarms().get(index);
	}

	/**
	 * @return The list of alarms.
	 */
	private List<NacAlarm> getAlarms()
	{
		return this.mAlarmList;
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
		return this.get(position).getId();
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
		return this.mNextAlarm;
	}

	/**
	 * @return The notification.
	 */
	public NacUpcomingAlarmNotification getNotification()
	{
		return this.mNotification;
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
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mShared;
	}

	/**
	 * @return The snackbar.
	 */
	private NacSnackbar getSnackbar()
	{
		return this.mSnackbar;
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
	 * @return True if the next calendar has been found, and False otherwise.
	 */
	private boolean isNextAlarm(NacAlarm alarm)
	{
		NacAlarm nextAlarm = this.getNextAlarm();
		Calendar nextCalendar = NacCalendar.getNext(nextAlarm);
		Calendar calendar = NacCalendar.getNext(alarm);

		return ((nextCalendar == null)
			|| ((calendar != null) && !calendar.after(nextCalendar)));
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
		NacAlarm alarm = this.get(position);

		alarm.setOnChangeListener(this);
		card.init(alarm);
		card.setOnDeleteListener(this);

		if (this.wasAddedWithFloatingButton())
		{
			card.interact();
		}

		this.setWasAddedWithFloatingButton(false);
	}

	/**
	 * Update the database when alarm data is changed.
	 *
	 * @param  a  The alarm that was changed.
	 */
	@Override
	public void onChange(NacAlarm alarm)
	{
		Context context = this.getContext();
		Intent intent = NacIntent.createService(context, "update", alarm);

		if (alarm.wasChanged())
		{
			List<NacAlarm> alarms = this.getAlarms();
			NacAlarm nextAlarm = NacCalendar.getNextAlarm(alarms);

			if (alarm.getEnabled())
			{
				this.showAlarm(alarm);
			}
			else
			{
				this.showNextAlarm(nextAlarm);
			}

			this.mNextAlarm = nextAlarm;
		}

		this.setWasAddedWithFloatingButton(false);
		this.updateNotification();
		this.startService(intent);
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
			this.delete(position);
		}
		else if (type == Undo.Type.DELETE)
		{
			this.restore(alarm, position);
		}
		else if (type == Undo.Type.RESTORE)
		{
			this.delete(position);
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
		this.delete(position);
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
		this.copy(position);
	}

	/**
	 * Delete the alarm.
	 * 
	 * @param  pos	The position of the alarm to delete.
	 */
	@Override
	public void onItemDelete(int position)
	{
		this.delete(position);
	}

	/**
	 * Drag and reorder the alarm.
	 *
	 * @param  from  The position that the alarm is moving from.
	 * @param  to  The position the alarm is moving to.
	 */
	@Override
	public void onItemMove(int fromIndex, int toIndex)
	{
		Context context = this.getContext();
		NacAlarm fromAlarm = this.get(fromIndex);
		NacAlarm toAlarm = this.get(toIndex);
		Intent intent = NacIntent.createService(context, "swap", fromAlarm,
			toAlarm);

		this.setWasAddedWithFloatingButton(false);
		this.startService(intent);
		Collections.swap(this.getAlarms(), fromIndex, toIndex);
		notifyItemMoved(fromIndex, toIndex);
	}

	/**
	 * @note Needed for RecyclerView.OnItemTouchListener
	 */
	public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept)
	{
	}

	/**
	 * @note Needed for RecyclerView.OnItemTouchListener
	 */
	public void onTouchEvent(RecyclerView rv, MotionEvent e)
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
		int result = this.add(alarm, position);

		if (result == 0)
		{
			this.undo(alarm, position, Undo.Type.RESTORE);
			this.snackbar("Restored alarm.");
		}

		this.setWasAddedWithFloatingButton(false);
	}

	/**
	 * Set whether an alarm was added with the floating button.
	 */
	public void setWasAddedWithFloatingButton(boolean added)
	{
		this.mWasAddedWithFloatingButton = added;
	}

	/**
	 * Show next alarm.
	 */
	private void showAlarm(NacAlarm alarm)
	{
		Calendar alarmCalendar = NacCalendar.getNext(alarm);
		Calendar previousCalendar = this.getPreviousCalendar();

		if ((alarm == null) || (alarmCalendar.equals(previousCalendar))
			|| !alarm.getEnabled())
		{
			return;
		}

		NacSharedPreferences shared = this.getSharedPreferences();
		String name = alarm.getName();
		String prefix = "Will run";

		if ((name != null) && !name.isEmpty())
		{
			prefix = (name.length() > 15) ? String.format("\"%12s...\"", name)
				: String.format("\"%s\"", name);
			prefix += " will run";
		}

		String message = NacCalendar.getMessage(prefix, shared, alarm);
		this.mPreviousCalendar = alarmCalendar;

		this.snackbar(message, "DISMISS", null, true);
	}

	private void showNextAlarm(NacAlarm alarm)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		String prefix = "Next alarm";
		String message = NacCalendar.getMessage(prefix, shared, alarm);

		this.snackbar(message, "DISMISS", null, true);
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
		this.snackbar(message, "UNDO", this, true);
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
	 * Start a background service.
	 */
	private void startService(Intent intent)
	{
		Context context = this.getContext();

		try
		{
			context.startService(intent);
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("NacCardAdapter : IllegalStateException caught in startService()");
			return;
		}
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
			NacUtility.printf("updateNotification!");
			this.getNotification().update(alarms);
		}
	}

	/**
	 * @return True if the alarm was added, and False otherwise.
	 */
	public boolean wasAddedWithFloatingButton()
	{
		return this.mWasAddedWithFloatingButton;
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
