package com.nfcalarmclock;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class AlarmAdapter
    extends RecyclerView.Adapter<AlarmAdapter.MyViewHolder>
{

    private Context mContext;
    private List<Alarm> alarmList;

    public class MyViewHolder
        extends RecyclerView.ViewHolder
    {
        public ImageView toggleAlarm;
        public ImageView overflow;
        public TextView alarmTime;
        public TextView alarmInfo;

        public MyViewHolder(View view)
        {
            super(view);
            toggleAlarm = (ImageView) view.findViewById(R.id.toggleAlarm);
            alarmTime = (TextView) view.findViewById(R.id.alarmTime);
            alarmInfo = (TextView) view.findViewById(R.id.alarmInfo);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }

    public AlarmAdapter(Context mContext)
    {
        this.mContext = mContext;
        this.alarmList = new ArrayList<>();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.view_card_alarm, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)
    {
        Alarm alarm = alarmList.get(position);
        holder.alarmTime.setText(alarm.getName());
        holder.alarmInfo.setText(alarm.getType() + " songs");
        holder.overflow.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showPopupMenu(holder.overflow);
                }
            });
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
        return alarmList.size();
    }

    public List<Alarm> getAlarms()
    {
        return alarmList;
    }

}
