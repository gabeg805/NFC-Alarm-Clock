package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
	 * List of alarms.
	 */
	private List<NacAlarm> mAlarmList;

	/**
	 * Previously enabled alarm.
	 */
	private NacAlarm mNextAlarm;

	/**
	 * Indicator that the alarm was added through the floating action button.
	 */
	//private boolean mWasAdded;
	private List<NacCardHolder.State> mStateList;

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
		this.mTouchHelper = new NacCardTouchHelper(callback);
		this.mUndo = new Undo();
		this.mSnackbar = new NacSnackbar(this.mRoot);
		this.mAlarmList = null;
		this.mStateList = null;
		this.mNextAlarm = null;
		//this.mWasAdded = false;
		this.mMeasure = new NacCardMeasure(context);

		this.mRecyclerView.addOnItemTouchListener(this);
		//setHasStableIds(true);
	}

	/**
	 * Add an alarm.
	 */
	public int add()
	{
		Context context = this.getContext();
		int id = this.getUniqueId();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		NacAlarm alarm = new NacAlarm.Builder()
			.setId(id)
			.setRepeat(shared.getRepeat())
			.setDays(shared.getDays())
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
		//this.mWasAdded = true;

		this.startService(intent);
		this.getAlarms().add(position, alarm);
		this.mStateList.add(position, NacCardHolder.State.ADDED);
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
		NacSharedPreferences shared = new NacSharedPreferences(context);
		NacDatabase db = new NacDatabase(context);
		this.mAlarmList = db.read();
		this.mStateList = new ArrayList<>();
		NacUtility.printf("BUILDING");

		if (shared.getAppFirstRun())
		{
			for (NacAlarm a : this.getAlarms())
			{
				this.onChange(a);
			}

			shared.editAppFirstRun(false);
		}

		for (NacAlarm a : this.getAlarms())
		{
			this.mStateList.add(NacCardHolder.State.CREATED);
		}

		this.getTouchHelper().setRecyclerView(this.getRecyclerView());
		this.getTouchHelper().reset();
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
		//this.mWasAdded = false;
		this.mStateList.set(position, NacCardHolder.State.COPIED);

		if (result == 0)
		{
			this.undo(copy, this.size()-1, Undo.Type.COPY);
			this.snackbar("Copied alarm.");
		}

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

		this.startService(intent);
		this.getAlarms().remove(position);
		this.mStateList.remove(position);
		notifyItemRemoved(position);
		this.undo(alarm, position, Undo.Type.DELETE);
		this.snackbar("Deleted alarm.");

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
	 */
	//@Override
	//public long getItemId(int position)
	//{
	//	NacUtility.printf("Item ID : %d %d", position, this.get(position).getId());
	//	return this.get(position).getId();
	//	//return position;
	//}

	/**
	 * @return The number of items in the recycler view.
	 */
	@Override
	public int getItemCount()
	{
		return this.size();
	}

	/**
	 */
	//@Override
	//public int getItemViewType(int position)
	//{
	//	return position;
	//}

	/**
	 * @return The next alarm that will be triggered.
	 */
	private NacAlarm getNextAlarm()
	{
		return this.mNextAlarm;
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
	private int getUniqueId()
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
	//private boolean isNextCalendar(Calendar calendar)
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
		NacUtility.printf("onBindViewHolder! %s", this.mStateList.get(position).toString());
		NacAlarm alarm = this.get(position);

		alarm.setOnChangeListener(this);
		card.init(alarm, this.mStateList.get(position));
		card.setOnDeleteListener(this);

		this.mStateList.set(position, NacCardHolder.State.NONE);
		//this.mWasAdded = false;
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

		if (alarm.wasEnabled())
		{
			this.showNextAlarm(alarm);
		}
		else
		{
			if (this.areAllAlarmsDisabled())
			{
				this.mNextAlarm = null;
			}
		}

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

		//NacUtility.printf("onInterceptTouch! %d %f %f", ev.getAction(), ev.getX(), ev.getY());
		//View view = rv.findChildViewUnder(ev.getX(), ev.getY());

		//if (view == null)
		//{
		//	NacUtility.printf("View is NULL!");
		//	return false;
		//}

		////NacCardHolder holder = (NacCardHolder) rv.findContaingViewHolder(view);
		//NacCardHolder holder = (NacCardHolder) rv.getChildViewHolder(view);

		//if (holder == null)
		//{
		//	NacUtility.printf("HOLDER is NULL!");
		//}
		//else if(holder.isExpanded())
		//{
		//	NacUtility.printf("View is expanded!");
		//	//view.getParent().requestDisallowInterceptTouchEvent(true);
		//}
		//else
		//{
		//	NacUtility.printf("View is collapsed!");
		//}

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

		this.startService(intent);
		Collections.swap(this.getAlarms(), fromIndex, toIndex);
		this.mStateList.set(fromIndex, NacCardHolder.State.MOVED);
		this.mStateList.set(toIndex, NacCardHolder.State.MOVED);
		notifyItemMoved(fromIndex, toIndex);
	}

	/**
	 * @note Needed for RecyclerView.OnItemTouchListener
	 */
	public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept)
	{
		NacUtility.printf("onRequestDisallowInterceptTouchEvent! %b", disallowIntercept);
	}

	/**
	 * @note Needed for RecyclerView.OnItemTouchListener
	 */
	public void onTouchEvent(RecyclerView rv, MotionEvent e)
	{
	}

	/**
	 * Measure the expanded and collapsed sizes of the alarm card.
	 */
	@Override
	public void onViewAttachedToWindow(NacCardHolder card)
	{
		//NacUtility.printf("Measuring card! %d", card.getAdapterPosition());
		//card.measure();
	}

	/**
	 * Clear any animation that is occuring.
	 */
	@Override
	public void onViewDetachedFromWindow(NacCardHolder card)
	{
		//card.unfocus();
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
		this.mStateList.set(position, NacCardHolder.State.DELETED);

		if (result == 0)
		{
			this.undo(alarm, position, Undo.Type.RESTORE);
			this.snackbar("Restored alarm.");
		}
	}

	/**
	 * Show next alarm.
	 */
	private void showNextAlarm(NacAlarm alarm)
	{
		NacAlarm nextAlarm = this.getNextAlarm();
		int nextId = (nextAlarm != null) ? nextAlarm.getId() : -1;
		int id = alarm.getId();

		if (id == nextId)
		{
			alarm = NacCalendar.getNextAlarm(this.getAlarms());
		}
		else if (!this.isNextAlarm(alarm))
		{
			return;
		}

		Context context = this.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		Calendar calendar = NacCalendar.getNext(alarm);
		boolean timeRemaining = shared.getDisplayTimeRemaining();
		long millis = calendar.getTimeInMillis();
		String message = NacCalendar.getNextMessage(millis, timeRemaining);
		this.mNextAlarm = alarm;

		this.snackbar(message, "DISMISS", null, false);
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
