package com.nfcalarmclock;

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

/**
 * @brief Alarm card adapter.
 */
public class NacCardAdapter
	extends RecyclerView.Adapter<NacCard>
	implements View.OnClickListener,Alarm.OnChangedListener,NacCardTouchHelperAdapter
{

	/**
	 * @brief The Context of the parent.
	 */
	private Context mContext = null;

	/**
	 * @brief The recycler view.
	 */
	private RecyclerView mRecyclerView = null;

	/**
	 * @brief Alarm scheduler.
	 */
	private NacAlarmScheduler mScheduler = null;

	/**
	 * @brief List of alarms.
	 */
	private List<Alarm> mAlarmList;

	/**
	 * @brief The database.
	 */
	private NacDatabase mDatabase = null;

	/**
	 * @brief Number of alarm cards in the recycler view.
	 */
	private int mSize = 0;

	/**
	 * @brief Indicator that the alarm was added through the floating action button.
	 */
	private boolean mWasAdded = false;

	/**
	 * @brief Handle card swipe events.
	 */
	private ItemTouchHelper mTouchHelper = null;

	/**
	 * @brief The alarm to restore, when prompted after deletion.
	 */
	private Alarm mRestoreAlarm = null;

	/**
	 * @brief The position of the alarm to restore, when prompted after
	 *        deletion.
	 */
	private int mRestorePosition = -1;

	/**
	 * @brief Alarm adapter.
	 *
	 * @param  c  The activity context.
	 */
	public NacCardAdapter(Context c)
	{
		this.mContext = c;
		this.mScheduler = new NacAlarmScheduler(c);
		this.mDatabase = new NacDatabase(c);

		this.setTouchHelper();
	}

	private void setTouchHelper()
	{
		RecyclerView rv = this.mRecyclerView;
		
		if (rv == null)
		{
			AppCompatActivity a = (AppCompatActivity) this.mContext;
			rv = (RecyclerView) a.findViewById(R.id.content_alarm_list);
		}

		if (this.mTouchHelper == null)
		{
			ItemTouchHelper.Callback callback = new NacCardTouchHelperCallback(this);
			this.mTouchHelper = new ItemTouchHelper(callback);
		}

		this.mTouchHelper.attachToRecyclerView(null);
		this.mTouchHelper.attachToRecyclerView(rv);
	}

	/**
	 * @brief Build the alarm list.
	 */
	public void build()
	{
		this.mDatabase.print();
		this.mAlarmList = this.mDatabase.read();
		this.mSize = this.mAlarmList.size();
		this.notifyDataSetChanged();
	}

	/**
	 * @brief Add an alarm.
	 */
	public void add()
	{
		boolean format = DateFormat.is24HourFormat(this.mContext);
		int id = this.getUniqueId();
		Alarm alarm = new Alarm(format, id);

		this.add(alarm);
	}

	/**
	 * @brief Add an alarm.
	 *
	 * @param  alarm  The alarm to add.
	 */
	public void add(Alarm a)
	{
		if (this.mDatabase.add(a) < 0)
		{
			Toast.makeText(this.mContext, "Error occurred when adding alarm to database.",
				Toast.LENGTH_SHORT).show();
			return;
		}

		a.print();
		// Using update instead of add for testing. Things should never get
		// canceled in update, only added
		this.mScheduler.update(a);
		this.mAlarmList.add(a);
		this.resize();
		this.notifyItemInserted(this.mSize-1);
		this.scrollToAlarm(this.mSize-1);

		this.mWasAdded = true;
	}

	/**
	 * @brief Delete the alarm at the given position.
	 *
	 * @param  pos	The card position of the alarm to delete.
	 */
	public void delete(int pos)
	{
		Alarm alarm = this.mAlarmList.get(pos);
		NacUtility.printf("Removing alarm at position %d.", pos);
		alarm.print();

		this.mScheduler.cancel(alarm);
		this.mDatabase.delete(alarm);
		this.mAlarmList.remove(pos);
		this.resize();
		this.notifyItemRemoved(pos);
		this.notifyItemRangeChanged(pos, this.getLastVisible(pos));
	}

	/**
	 * @brief Restore a previously deleted alarm.
	 * 
	 * @param  a  The alarm to restore.
	 * @param  pos  The position to insert the alarm.
	 */
	public void restore(Alarm a, int pos)
	{
		NacUtility.printf("Undoing alarm deletion! Position = %d", pos);
		this.mWasAdded = true;

		this.mAlarmList.add(pos, a);
		this.resize();
		this.notifyItemInserted(pos);
	}

	/**
	 * @brief Resize the alarm list.
	 */
	private void resize()
	{
		this.mSize = this.mAlarmList.size();
	}

	/**
	 * @brief Scroll to the alarm.
	 */
	private void scrollToAlarm(int index)
	{
		this.mRecyclerView.scrollToPosition(index);
	}

	/**
	 * @brief Determine a unique integer ID number to use for newly created
	 *		  alarms.
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
	 * @return The last visible view holder in the recycler view.
	 */
	private int getLastVisible(int start)
	{
		for (int i=start; i < this.mSize; i++)
		{
			if (this.mRecyclerView.findViewHolderForAdapterPosition(i) == null)
			{
				return i-1;
			}
		}

		return -1;
	}

	@Override
	public void onItemCopy(int pos)
	{
		this.setTouchHelper();
		NacUtility.printf("Item copy %d.", pos);

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
		Toast.makeText(this.mContext, "Copied alarm.",
			Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onItemDelete(int pos)
	{
		NacUtility.printf("Item delete %d.", pos);
		this.mRestoreAlarm = this.mAlarmList.get(pos);
		this.mRestorePosition = pos;

		this.delete(pos);

		AppCompatActivity a = (AppCompatActivity) this.mContext;
		View root = a.findViewById(R.id.activity_main);
		int color = NacUtility.getThemeAttrColor(this.mContext,
			R.attr.colorCardAccent);
		Snackbar snackbar = Snackbar.make(root, "Deleted alarm.",
			Snackbar.LENGTH_LONG);

		snackbar.setAction("UNDO", this);
		snackbar.setActionTextColor(color);
		snackbar.show();
	}

	/**
	 * @brief Update the database when alarm data is changed.
	 *
	 * @param  a  The alarm object that was changed.
	 */
	@Override
	public void onChanged(Alarm a)
	{
		this.mDatabase.update(a);
		this.mScheduler.update(a);
	}

	/**
	 * @brief Create the view holder.
	 *
	 * @param  parent  The parent view.
	 * @param  viewType  The type of view.
	 */
	@Override
	public NacCard onCreateViewHolder(ViewGroup parent, int viewType)
	{
		Context context = parent.getContext();
		int layout = R.layout.view_card_alarm;
		View root = LayoutInflater.from(context).inflate(layout, parent,
			false);

		return new NacCard(context, root);
	}

	/**
	 * @brief Bind the view holder.
	 *
	 * @param  card  The alarm card.
	 * @param  pos	The position of the alarm card.
	 */
	@Override
	public void onBindViewHolder(final NacCard card, int pos)
	{
		Alarm alarm = mAlarmList.get(pos);
		NacUtility.printf("onBindViewHolder %d", pos);

		alarm.setOnChangedListener(this);
		card.init(alarm);
		card.setDeleteListener(this);
		card.focus(this.mWasAdded);

		this.mWasAdded = false;
	}

	/**
	 * @brief Capture the click event on the delete button, and delete the card
	 *		  it belongs to.
	 *
	 * @param  v  The view that was clicked.
	 */
	@Override
	public void onClick(View v)
	{
		Object tag = v.getTag();

		if (tag == null)
		{
			restore(mRestoreAlarm, mRestorePosition);
		}
		else
		{
			delete((int)tag);
		}
	}

	/**
	 * @brief Set the recycler view.
	 */
	@Override
	public void onAttachedToRecyclerView(RecyclerView rv)
	{
		super.onAttachedToRecyclerView(rv);

		mRecyclerView = rv;
	}

	/**
	 * @brief Return the number of items in the recycler view.
	 */
	@Override
	public int getItemCount()
	{
		return this.mSize;
	}

}
