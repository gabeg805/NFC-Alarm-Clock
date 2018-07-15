package com.nfcalarmclock;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief Alarm card adapter.
 */
public class NacCardAdapter
    extends RecyclerView.Adapter<NacCard>
	implements View.OnClickListener
{

    /**
     * @brief The Activity of the parent.
     */
    private AppCompatActivity mActivity;

    /**
     * @brief The Context of the parent.
     */
    private Context mContext;

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
    private int mSize;

    /**
     * @brief Alarm adapter.
     */
    public NacCardAdapter(Context c)
    {
        this.mActivity = (AppCompatActivity) c;
        this.mContext = c;
        this.mDatabase = new NacDatabase(c);
    }

	public void unitTest()
	{
        Alarm a1 = new Alarm(11, 0);
        Alarm a2 = new Alarm(12, 0);
        Alarm a3 = new Alarm(13, 0);
        a1.setVibrate(true);
        a2.setDays(Alarm.Days.SATURDAY|Alarm.Days.SUNDAY);
        a3.setDays(Alarm.Days.TUESDAY|Alarm.Days.WEDNESDAY|Alarm.Days.THURSDAY);
        a3.setName("Oh Yeah!");
        this.mDatabase.add(a1);
        this.mDatabase.add(a2);
        this.mDatabase.add(a3);
	}

    /**
     * @brief Build the alarm list.
     */
    public void build()
    {
		unitTest();
        this.mAlarmList = this.mDatabase.read();
        this.mSize = this.mAlarmList.size();
    }

    /**
     * @brief Add an alarm.
     */
    public void add(Alarm alarm)
    {
        this.mSize += 1;
        this.mAlarmList.add(alarm);
        this.notifyItemInserted(this.mSize);
		((RecyclerView)mActivity.findViewById(R.id.content_alarm_list)).scrollToPosition(this.mSize-1);
    }

	/**
	 * @brief Delete the alarm at the given position.
	 */
	public void delete(int pos)
	{
        this.mSize -= 1;
        mAlarmList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(0, this.mSize);
	}

    /**
     * @brief Create the view holder.
     */
    @Override
    public NacCard onCreateViewHolder(ViewGroup parent, int viewType)
    {
        NacUtility.printf("onCreateViewHolder was called.");
        Context context = parent.getContext();
        int layout = R.layout.view_card_alarm;
        View root = LayoutInflater.from(context).inflate(layout, parent,
                                                         false);
        NacCard card = new NacCard(context, root);
		card.focus();
		card.setDeleteListener(this);
		card.setHeight();
		return card;
    }

    /**
     * @brief Bind the view holder.
     */
    @Override
    public void onBindViewHolder(final NacCard card, int pos)
    {
        NacUtility.printf("onBindViewHolder was called at position: %d", pos);
        Alarm alarm = mAlarmList.get(pos);
        card.init(alarm, pos);
        //card.collapse();
    }

    /**
     * @brief Capture the click event on the delete button, and delete the card
	 *        it belongs to.
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
