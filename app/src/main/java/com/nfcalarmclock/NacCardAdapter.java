package com.nfcalarmclock;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;

/**
 * @brief Alarm card adapter.
 */
public class NacCardAdapter
	extends RecyclerView.Adapter<NacCardHolder>
	implements View.OnClickListener,Alarm.OnChangedListener,NacCardHolder.OnDeleteListener,NacCardTouchHelper.Adapter
{

	/**
	 * Main activity root view.
	 */
	private CoordinatorLayout mRootView;

	/**
	 * RecyclerView containing list of alarm cards.
	 */
	private RecyclerView mRecyclerView;

	/**
	 * Alarm scheduler.
	 */
	private NacAlarmScheduler mScheduler;

	/**
	 * The database.
	 */
	private NacDatabase mDatabase;

	/**
	 * The alarm to restore, when prompted after deletion.
	 */
	private NacCardUndo mUndo;

	/**
	 * Handle card swipe events.
	 */
	private NacCardTouchHelper mTouchHelper;

	/**
	 * List of alarms.
	 */
	private List<Alarm> mAlarmList;

	/**
	 * Indicator that the alarm was added through the floating action button.
	 */
	private boolean mWasAdded;

	/**
	 */
	public NacCardAdapter(Context context)
	{
		AppCompatActivity activity = (AppCompatActivity) context;
		NacCardTouchHelper.Callback callback =
			new NacCardTouchHelper.Callback(this);

		this.mRootView = activity.findViewById(R.id.activity_main);
		this.mRecyclerView = (RecyclerView) this.mRootView.findViewById(
			R.id.content_alarm_list);
		this.mScheduler = new NacAlarmScheduler(context);
		this.mDatabase = new NacDatabase(context);
		this.mUndo = new NacCardUndo();
		this.mTouchHelper = new NacCardTouchHelper(callback);
		this.mAlarmList = null;
		this.mWasAdded = false;
	}

	/**
	 * Add an alarm.
	 */
	public void add()
	{
		Context context = this.mRootView.getContext();
		boolean format = DateFormat.is24HourFormat(context);
		int id = this.getUniqueId();
		Alarm alarm = new Alarm(format, id);

		this.add(alarm);
	}

	/**
	 * @see add
	 *
	 * @param  alarm  The alarm to add.
	 */
	public void add(Alarm alarm)
	{
		int index = this.mAlarmList.size();

		this.add(alarm, index);
		this.mRecyclerView.scrollToPosition(index);
	}

	/**
	 * @see add
	 *
	 * @param  alarm  The alarm to add.
	 * @param  position  The position to add the alarm.
	 */
	public void add(Alarm alarm, int position)
	{
		if (this.mDatabase.add(alarm) < 0)
		{
			NacUtility.snackbar(this.mRootView,
				"Error occurred when adding alarm to database.",
				"DISMISS", null);
			return;
		}

		// Using update instead of add for testing. Things should never get
		// canceled in update, only added
		this.mWasAdded = true;

		this.mScheduler.update(alarm);
		this.mAlarmList.add(alarm);
		this.notifyItemInserted(position);
		//this.mRecyclerView.scrollToPosition(position);
	}

	/**
	 * Build the alarm list.
	 */
	public void build()
	{
		this.mAlarmList = this.mDatabase.read();

		this.mTouchHelper.setRecyclerView(this.mRecyclerView);
		this.mTouchHelper.reset();
		this.notifyDataSetChanged();
	}

	/**
	 * Delete the alarm at the given position.
	 *
	 * @param  pos	The card position of the alarm to delete.
	 */
	public void delete(int pos)
	{
		Alarm alarm = this.mAlarmList.get(pos);

		this.mScheduler.cancel(alarm);
		this.mDatabase.delete(alarm);
		this.mAlarmList.remove(pos);
		this.notifyItemRemoved(pos);
		this.notifyItemRangeChanged(pos, this.getLastVisible(pos));
		this.undo(alarm, pos, NacCardUndo.Type.DELETE);
		NacUtility.snackbar(this.mRootView, "Deleted alarm.", "UNDO", this);
	}

	/**
	 * @return The number of items in the recycler view.
	 */
	@Override
	public int getItemCount()
	{
		return this.mAlarmList.size();
	}

	/**
	 * @return The last visible view holder in the recycler view.
	 */
	private int getLastVisible(int start)
	{
		int size = this.mAlarmList.size();

		for (int i=start; i < size; i++)
		{
			if (this.mRecyclerView.findViewHolderForAdapterPosition(i) == null)
			{
				return i-1;
			}
		}

		return -1;
	}

	/**
	 * Determine a unique integer ID number to use for newly created alarms.
	 */
	private int getUniqueId()
	{
		List<Integer> used = new ArrayList<>();

		for (Alarm a : this.mAlarmList)
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
	 * Setup the alarm card.
	 *
	 * @param  card  The alarm card.
	 * @param  pos	The position of the alarm card.
	 */
	@Override
	public void onBindViewHolder(final NacCardHolder card, int pos)
	{
		Alarm alarm = mAlarmList.get(pos);

		alarm.setOnChangedListener(this);
		card.init(alarm, this.mWasAdded);
		card.setOnDeleteListener(this);
		//card.focus(this.mWasAdded);

		this.mWasAdded = false;
	}

	/**
	 * Update the database when alarm data is changed.
	 *
	 * @param  a  The alarm that was changed.
	 */
	@Override
	public void onChanged(Alarm a)
	{
		this.mDatabase.update(a);
		this.mScheduler.update(a);
	}

	/**
	 * Capture the click event on the delete button, and delete the card it
	 * belongs to.
	 *
	 * @param  v  The view that was clicked.
	 */
	@Override
	public void onClick(View v)
	{
		Alarm alarm = this.mUndo.alarm;
		int position = this.mUndo.position;
		NacCardUndo.Type type = this.mUndo.type;

		this.mUndo.reset();

		if (type == NacCardUndo.Type.COPY)
		{
			this.delete(position);
		}
		else if (type == NacCardUndo.Type.DELETE)
		{
			this.restore(alarm, position);
		}
		else if (type == NacCardUndo.Type.RESTORE)
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
		int layout = R.layout.view_card_alarm;
		View root = LayoutInflater.from(context).inflate(layout, parent,
			false);

		return new NacCardHolder(root);
	}

	/**
	 * Delete the alarm.
	 */
	@Override
	public void onDelete(int pos)
	{
		this.delete(pos);
	}

	/**
	 * Copy the alarm.
	 *
	 * @param  pos  The position of the alarm to copy.
	 */
	@Override
	public void onItemCopy(int pos)
	{
		Alarm alarm = this.mAlarmList.get(pos);
		Alarm copy = alarm.copy();
		int newpos = this.mAlarmList.size();

		copy.setId(this.getUniqueId());
		this.mTouchHelper.reset();
		this.add(copy);
		this.undo(copy, newpos, NacCardUndo.Type.COPY);
		NacUtility.snackbar(this.mRootView, "Copied alarm.", "UNDO", this);
	}

	/**
	 * Delete the alarm.
	 * 
	 * @param  pos  The position of the alarm to delete.
	 */
	@Override
	public void onItemDelete(int pos)
	{
		this.delete(pos);
	}

	/**
	 * Restore a previously deleted alarm.
	 * 
	 * @param  alarm  The alarm to restore.
	 * @param  position  The position to insert the alarm.
	 */
	public void restore(Alarm alarm, int position)
	{
		this.add(alarm, position);
		this.undo(alarm, position, NacCardUndo.Type.RESTORE);
		NacUtility.snackbar(this.mRootView, "Restored alarm.", "UNDO", this);
	}

	/**
	 * Save undo parameters.
	 */
	public void undo(Alarm alarm, int position, NacCardUndo.Type type)
	{
		this.mUndo.set(alarm, position, type);
	}

}
