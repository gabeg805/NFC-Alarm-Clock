package com.nfcalarmclock;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class AlarmAdapter
    extends RecyclerView.Adapter<AlarmAdapter.MyViewHolder>
{

    /**
     * @brief The Context of the parent.
     */
    private Context mContext;

    /**
     * @brief List of Alarms.
     */
    private List<Alarm> mAlarmList;

    /**
     * 
     */
    public class MyViewHolder
        extends RecyclerView.ViewHolder
    {
        public TextView alarmTime;
        public TextView alarmTimeMeridian;
        public TextView alarmName;
        public ImageView musicIcon;
        public Switch alarmSwitch;

        public MyViewHolder(View view)
        {
            super(view);
            alarmTime = (TextView) view.findViewById(R.id.alarmTime);
            alarmTimeMeridian = (TextView) view.findViewById(R.id.alarmTimeMeridian);
            alarmName = (TextView) view.findViewById(R.id.alarmName);
            musicIcon = (ImageView) view.findViewById(R.id.musicIcon);
            alarmSwitch = (Switch) view.findViewById(R.id.alarmSwitch);
        }
    }

    /**
     * @brief Alarm adapter.
     */
    public AlarmAdapter(Context context)
    {
        this.mContext = context;
        this.mAlarmList = new ArrayList<>();
    }

    /**
     * @brief Create the view holder.
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.view_card_alarm, parent, false);
        return new MyViewHolder(itemView);
    }

    /**
     * @brief Bind the view holder.
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)
    {
        Alarm alarm = mAlarmList.get(position);
        boolean is24hourformat = DateFormat.is24HourFormat(mContext);
        is24hourformat = false;
        String hour = String.valueOf(alarm.toFormat(alarm.getHour(),
                                                    is24hourformat));
        String minute = String.format("%02d", alarm.getMinute());
        String meridian = alarm.getMeridian(alarm.getHour(), is24hourformat);

        Toast.makeText(mContext, hour+":"+minute+meridian+"~", Toast.LENGTH_SHORT).show();

        holder.alarmTime.setText(hour+":"+minute);
        holder.alarmTimeMeridian.setText(meridian);
        holder.alarmName.setText(alarm.getName());

        // holder.overflow.setOnClickListener(new View.OnClickListener()
        //     {
        //         @Override
        //         public void onClick(View view)
        //         {
        //             showPopupMenu(holder.overflow);
        //         }
        //     });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view)
    {
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_card_alarm, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener
        implements PopupMenu.OnMenuItemClickListener
    {

        public MyMenuItemClickListener()
        {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
            case R.id.action_add_favourite:
                Toast.makeText(mContext, "Add to favourite", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_play_next:
                Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
                return true;
            default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount()
    {
        return mAlarmList.size();
    }

    public List<Alarm> getAlarms()
    {
        return mAlarmList;
    }

}
