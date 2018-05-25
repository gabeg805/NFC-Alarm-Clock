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

// For expandable card content
import android.widget.RelativeLayout;

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
        public TextView alarmMeridian;
        public TextView alarmRepeatText;
        public Switch alarmSwitch;
        public ImageView alarmExpandButton;
        public ImageView alarmCollapseButton;
        public RelativeLayout alarmMinorSummary;
        public RelativeLayout alarmMinorExpand;

        public MyViewHolder(View view)
        {
            super(view);
            alarmTime = (TextView) view.findViewById(R.id.alarmTime);
            alarmMeridian = (TextView) view.findViewById(R.id.alarmMeridian);
            alarmRepeatText = (TextView) view.findViewById(R.id.alarmRepeatText);
            alarmSwitch = (Switch) view.findViewById(R.id.alarmSwitch);
            alarmExpandButton = (ImageView) view.findViewById(R.id.alarmExpandButton);
            alarmCollapseButton = (ImageView) view.findViewById(R.id.alarmCollapseButton);
            alarmMinorSummary = (RelativeLayout) itemView.findViewById(R.id.alarmMinorSummary);
            alarmMinorExpand = (RelativeLayout) itemView.findViewById(R.id.alarmMinorExpand);
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
        MyViewHolder holder = new MyViewHolder(itemView);
        holder.alarmMinorExpand.setVisibility(View.GONE);
        holder.alarmMinorExpand.setEnabled(false);
        return holder;
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

        holder.alarmTime.setText(hour+":"+minute);
        holder.alarmMeridian.setText(meridian);
        holder.alarmRepeatText.setText(alarm.getName());
        holder.alarmExpandButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(mContext, "Expanded.", Toast.LENGTH_SHORT).show();
                    holder.alarmMinorSummary.setVisibility(View.GONE);
                    holder.alarmMinorSummary.setEnabled(false);
                    holder.alarmMinorExpand.setVisibility(View.VISIBLE);
                    holder.alarmMinorExpand.setEnabled(true);
                }
            });
        holder.alarmCollapseButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(mContext, "Collapsed.", Toast.LENGTH_SHORT).show();
                    holder.alarmMinorSummary.setVisibility(View.VISIBLE);
                    holder.alarmMinorSummary.setEnabled(true);
                    holder.alarmMinorExpand.setVisibility(View.GONE);
                    holder.alarmMinorExpand.setEnabled(false);
                }
            });
    }

        //             PopupMenu popup = new PopupMenu(mContext, view);
        //             MenuInflater inflater = popup.getMenuInflater();
        //             inflater.inflate(R.menu.alarm_card, popup.getMenu());
        //             // popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        //             popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        //                 {
        //                     @Override
        //                     public boolean onMenuItemClick(MenuItem menuItem)
        //                     {
        //                         switch (menuItem.getItemId())
        //                         {
        //                         case R.id.alarm_card_edit:
        //                             Toast.makeText(mContext, "Edit alarm.", Toast.LENGTH_SHORT).show();
        //                             return true;
        //                         case R.id.alarm_card_copy:
        //                             Toast.makeText(mContext, "Copy alarm.", Toast.LENGTH_SHORT).show();
        //                             return true;
        //                         case R.id.alarm_card_delete:
        //                             Toast.makeText(mContext, "Delete alarm.", Toast.LENGTH_SHORT).show();
        //                             return true;
        //                         default:
        //                             break;
        //                         }
        //                         return false;
        //                     }
        //                 });
        //             popup.show();
        //             // showPopupMenu(holder.alarmMenu);

    // /**
    //  * Showing popup menu when tapping on 3 dots
    //  */
    // private void showPopupMenu(View view)
    // {
    // }

    // /**
    //  * Click listener for popup menu items
    //  */
    // class MyMenuItemClickListener
    //     implements PopupMenu.OnMenuItemClickListener
    // {

    //     public MyMenuItemClickListener()
    //     {
    //     }

    //     @Override
    //     public boolean onMenuItemClick(MenuItem menuItem)
    //     {
    //         switch (menuItem.getItemId())
    //         {
    //         case R.id.action_add_favourite:
    //             Toast.makeText(mContext, "Add to favourite", Toast.LENGTH_SHORT).show();
    //             return true;
    //         case R.id.action_play_next:
    //             Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
    //             return true;
    //         default:
    //         }
    //         return false;
    //     }
    // }

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
