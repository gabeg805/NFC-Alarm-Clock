package com.nfcalarmclock;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.content.Context;
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
	 * @brief The app activity.
	 */
	private AppCompatActivity mActivity = null;

	/**
	 * @brief The Context of the parent.
	 */
	private Context mContext = null;

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
	private NacDatabase mDatabase;

	/**
	 * @brief Number of alarm cards in the recycler view.
	 */
	private int mSize = 0;
	private int mFirstVisible = 0;
	private int mLastVisible = 0;

	/**
	 * @brief Indicator that the alarm was added through the floating action button.
	 */
	private boolean mWasAdded = false;

	private ItemTouchHelper mTouchHelper = null;

	/**
	 * @brief Alarm adapter.
	 *
	 * @param  c  The activity context.
	 */
	public NacCardAdapter(Context c)
	{
		this.mActivity = (AppCompatActivity) c;
		this.mContext = c;
		this.mScheduler = new NacAlarmScheduler(c);
		this.mDatabase = new NacDatabase(c);

		this.setItemTouchHelper();
	}

	private void setItemTouchHelper()
	{
		if (this.mTouchHelper == null)
		{
			ItemTouchHelper.Callback callback = new NacCardTouchHelperCallback(this);
			this.mTouchHelper = new ItemTouchHelper(callback);
		}

		RecyclerView rv = (RecyclerView) this.mActivity.findViewById(
			R.id.content_alarm_list);

		this.mTouchHelper.attachToRecyclerView(null);
		this.mTouchHelper.attachToRecyclerView(rv);
	}

	public void unitTest()
	{
		Alarm a1 = new Alarm(11, 0);
		Alarm a2 = new Alarm(12, 0);
		Alarm a3 = new Alarm(13, 0);

		a1.setOnChangedListener(this);
		a2.setOnChangedListener(this);
		a3.setOnChangedListener(this);
		a1.set24HourFormat(false);
		a2.set24HourFormat(false);
		a3.set24HourFormat(false);
		a1.setVibrate(true);
		a2.setDays(Alarm.Days.SATURDAY|Alarm.Days.SUNDAY);
		a3.setDays(Alarm.Days.TUESDAY|Alarm.Days.WEDNESDAY|Alarm.Days.THURSDAY);
		a3.setName("Oh Yeah!");
		a1.setSound("content://media/internal/audio/media/12");
		a2.setSound("/storage/emulated/0/Music/Alvvays - Dreams Tonite.mp3");
		a3.setSound("content://media/internal/audio/media/13");

		this.mDatabase.add(a1);
		this.mDatabase.add(a2);
		this.mDatabase.add(a3);
	}

	/**
	 * @brief Build the alarm list.
	 */
	public void build()
	{
		//unitTest();
		this.mDatabase.print();
		this.mAlarmList = this.mDatabase.read();
		this.mSize = this.mAlarmList.size();
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
			// Indicate visually that this is an error.
			return;
		}

		a.print();
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
		this.notifyItemRangeChanged(pos, this.mLastVisible);
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
		RecyclerView rv = (RecyclerView) this.mActivity.findViewById(
			R.id.content_alarm_list);

		rv.scrollToPosition(index);
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

	@Override
	public void onItemCopy(int pos)
	{
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
	}

	@Override
	public void onItemDelete(int pos)
	{
		NacUtility.printf("Item delete %d.", pos);
		this.delete(pos);

		// showing snack bar with Undo option
		Snackbar snackbar = Snackbar
				.make(((AppCompatActivity)mContext).findViewById(R.id.activity_main), " removed from cart!", Snackbar.LENGTH_LONG);
		snackbar.setAction("UNDO", new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				NacUtility.printf("Undoing remove!");
				// undo is selected, restore the deleted item
				//mAdapter.restoreItem(deletedItem, deletedIndex);
			}
		});
		snackbar.setActionTextColor(Color.YELLOW);
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
		NacCard card = new NacCard(context, root);

		return card;
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
		//NacUtility.printf("First visible=%d and last visible=%d", this.mFirstVisible, this.mLastVisible);

		alarm.setOnChangedListener(this);
		card.init(alarm);
		card.setDeleteListener(this);
		card.focus(this.mWasAdded);

		this.mWasAdded = false;
	}

	@Override
	public void onViewAttachedToWindow(NacCard holder)
	{
		int pos = holder.getAdapterPosition();

		if (pos > this.mLastVisible)
		{
			this.mLastVisible = pos;
		}
		else if (pos < this.mFirstVisible)
		{
			this.mFirstVisible = pos;
		}
		else
		{
			NacUtility.print("Why are positions equal to stuff???????");
		}

		NacUtility.printf("onViewAttachedToWindow %d. First=%d and Last=%d",
			pos, this.mFirstVisible, this.mLastVisible);
	}

	@Override
	public void onViewDetachedFromWindow(NacCard holder)
	{
		int pos = holder.getAdapterPosition();

		if (pos == this.mFirstVisible)
		{
			this.mFirstVisible = pos+1;
		}
		else if (pos == this.mLastVisible)
		{
			this.mLastVisible = pos-1;
		}
		else
		{
			NacUtility.print("Why are positions equal to stuff???????");
		}

		NacUtility.printf("onViewDetachedFromWindow %d. First=%d and Last=%d",
			pos, this.mFirstVisible, this.mLastVisible);
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
		int pos = (int) v.getTag();
		delete(pos);
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
