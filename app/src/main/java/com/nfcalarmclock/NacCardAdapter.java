package com.nfcalarmclock;

import android.support.design.widget.CoordinatorLayout;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.helper.ItemTouchHelper;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;

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
	 * The alarm to restore, when prompted after deletion.
	 */
	private Alarm mRestoreAlarm = null;

	/**
	 * The position of the alarm to restore, when prompted after deletion.
	 */
	private int mRestorePosition = -1;

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
	 * Add an alarm.
	 *
	 * @param  alarm  The alarm to add.
	 */
	public void add(Alarm a)
	{
		if (this.mDatabase.add(a) < 0)
		{
			Toast.makeText(this.mRootView.getContext(),
				"Error occurred when adding alarm to database.",
				Toast.LENGTH_SHORT).show();
			return;
		}

		// Using update instead of add for testing. Things should never get
		// canceled in update, only added
		int index = this.mAlarmList.size();
		this.mWasAdded = true;

		this.mScheduler.update(a);
		this.mAlarmList.add(a);
		this.notifyItemInserted(index);
		this.mRecyclerView.scrollToPosition(index);
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
		this.mRestoreAlarm = alarm;
		this.mRestorePosition = pos;

		this.mScheduler.cancel(alarm);
		this.mDatabase.delete(alarm);
		this.mAlarmList.remove(pos);
		this.notifyItemRemoved(pos);
		this.notifyItemRangeChanged(pos, this.getLastVisible(pos));

		Context context = this.mRootView.getContext();
		int color = NacUtility.getThemeAttrColor(context,
			R.attr.colorCardAccent);
		Snackbar snackbar = Snackbar.make(this.mRootView, "Deleted alarm.",
			Snackbar.LENGTH_LONG);

		snackbar.setAction("UNDO", this);
		snackbar.setActionTextColor(color);
		snackbar.show();
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
		Object tag = v.getTag();

		if (tag == null)
		{
			restore(this.mRestoreAlarm, this.mRestorePosition);
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
		this.mTouchHelper.reset();
		//this.resetTouchHelper();

		Alarm alarm = this.mAlarmList.get(pos);
		Alarm copy = new Alarm();

		copy.setId(this.getUniqueId());
		copy.setEnabled(alarm.getEnabled());
		copy.set24HourFormat(alarm.get24HourFormat());
		copy.setHour(alarm.getHour());
		copy.setMinute(alarm.getMinute());
		copy.setDays(alarm.getDays());
		copy.setRepeat(alarm.getRepeat());
		copy.setVibrate(alarm.getVibrate());
		copy.setSound(alarm.getSound());
		copy.setName(alarm.getName());
		this.add(copy);

		Context context = this.mRootView.getContext();
		Toast.makeText(context, "Copied alarm.",
			Toast.LENGTH_SHORT).show();
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
	 * @param  a  The alarm to restore.
	 * @param  pos  The position to insert the alarm.
	 */
	public void restore(Alarm a, int pos)
	{
		this.mWasAdded = true;

		this.mAlarmList.add(pos, a);
		this.notifyItemInserted(pos);
	}

}
