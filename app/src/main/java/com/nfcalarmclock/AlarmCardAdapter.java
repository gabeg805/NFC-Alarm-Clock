package com.nfcalarmclock;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
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
public class AlarmCardAdapter
    extends RecyclerView.Adapter<AlarmCard>
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
     * @brief Current adapter position.
     */
    private int mPosition;

    /**
     * @brief The RecyclerView containing the adapter.
     */
    private RecyclerView mRecyclerView;

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
    public AlarmCardAdapter(Context context)
    {
        this.mActivity = (AppCompatActivity) context;
        this.mContext = context;
        this.mRecyclerView = this.mActivity.findViewById(R.id.content_alarm_list);
        this.mDatabase = new NacDatabase(context);
    }

    /**
     * @brief Build the alarm list.
     */
    public void build()
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
    }

    /**
     * @brief Remove the alarm.
     */
    public void remove(int pos)
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
    public AlarmCard onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        int layout = R.layout.view_card_alarm;
        View root = LayoutInflater.from(context).inflate(layout, parent,
                                                         false);
        return new AlarmCard(this, context, root);
    }

    /**
     * @brief Bind the view holder.
     */
    @Override
    public void onBindViewHolder(final AlarmCard card, int position)
    {
        NacUtility.printf("onBindViewHolder was called at position: %d", position);
        Alarm alarm = mAlarmList.get(position);
        card.init(alarm);
        card.collapse();
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
