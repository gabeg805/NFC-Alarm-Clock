package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * @brief Holder of all important views.
 */
public class AlarmCard
    extends RecyclerView.ViewHolder
    implements View.OnCreateContextMenuListener
{

    public AlarmCardAdapter mAdapter;
    public AppCompatActivity activity;
    public Context mContext;
    public CardView mCard;
    // public ImageView expandbutton;
    public NacCardExpand mExpand;
    public NacCardCollapse mCollapse;
    public NacCardSwitch mSwitch;
    // public ImageView collapsebutton;
    public RelativeLayout summary;
    public RelativeLayout expandable;
    public NacCardTime mTime;
    // public RelativeLayout time;
    // public TextView hourminute;
    // public TextView meridian;
    // public Switch switched;
    public NacCardRepeat mRepeat;
    // public TextView repeattext;
    // public DayOfWeekButtons repeatdays;
    // public CheckBox repeat;
    // public ImageTextButton sound;
    public NacCardSound mSound;
    // public CheckBox vibrate;
    public NacCardVibrate mVibrate;
    // public ImageTextButton name;
    public NacCardName mName;
    public NacCardDelete mDelete;
    // public ImageTextButton delete;

    /**
     * @brief The alarm data used in populating this alarm card.
     */
    private Alarm mAlarm;

    /**
     * @brief The root view.
     */
    private View mRoot;

    /**
     * @brief Define all views in the holder.
     */
    public AlarmCard(AlarmCardAdapter adapter, Context c, View v)
    {
        super(v);
        this.mAdapter = adapter;
        this.mContext = c;
        this.mCard = (CardView) v.findViewById(R.id.view_card_alarm);
        this.mRoot = v;

        this.summary = (RelativeLayout) v.findViewById(R.id.alarmMinorSummary);
        this.expandable = (RelativeLayout) v.findViewById(R.id.alarmMinorExpand);

        this.mExpand = new NacCardExpand(this, c);
        this.mCollapse = new NacCardCollapse(this, c);
        this.mTime = new NacCardTime(this, c);
        this.mSwitch = new NacCardSwitch(this, c);
        this.mRepeat = new NacCardRepeat(this, c);
        this.mSound = new NacCardSound(this, c);
        this.mVibrate = new NacCardVibrate(this, c);
        this.mName = new NacCardName(this, c);
        this.mDelete = new NacCardDelete(this, c);

        AppCompatActivity activity = (AppCompatActivity) c;
        activity.registerForContextMenu(this.mCard);
        this.mCard.setOnCreateContextMenuListener(this);
    }

    /**
     * @brief Initialize the alarm card.
     */
    public void init(Alarm alarm)
    {
        this.mAlarm = alarm;
        this.expandable.setVisibility(View.GONE);
        this.expandable.setEnabled(false);
        this.mTime.init();
        this.mSwitch.init();
        this.mRepeat.init();
        this.mSound.init();
        this.mVibrate.init();
        this.mName.init();
    }

    /**
     * @brief Remove this alarm card.
     */
    public void remove()
    {
        int pos = this.getAdapterPosition();
        mAdapter.remove(pos);
    }

    /**
     * @brief Expand the alarm card.
     */
    public void expand()
    {
        this.summary.setVisibility(View.GONE);
        this.summary.setEnabled(false);
        this.expandable.setVisibility(View.VISIBLE);
        this.expandable.setEnabled(true);
        setColor(NacUtility.getThemeAttrColor(this.mContext,
                                              R.attr.colorCardExpanded));
    }

    /**
     * @brief Collapse the alarm card.
     */
    public void collapse()
    {
        this.summary.setVisibility(View.VISIBLE);
        this.summary.setEnabled(true);
        this.expandable.setVisibility(View.GONE);
        this.expandable.setEnabled(false);
        setColor(NacUtility.getThemeAttrColor(this.mContext,
                                              R.attr.colorCard));
    }

    /**
     * @brief Set the color of the card.
     */
    public void setColor(int color)
    {
        this.mCard.setCardBackgroundColor(color);
    }

    /**
     * @brief Set the color of the card using the resource ID.
     */
    public void setResourceColor(int id)
    {
        Resources r = mContext.getResources();
        int c = r.getColor(id);
        this.setColor(c);
    }

    /**
     * @brief Return the alarm associated with this card.
     * 
     * @return The alarm associated with this card.
     */
    public Alarm getAlarm()
    {
        return this.mAlarm;
    }

    /**
     * @brief Return the root view.
     * 
     * @return The root view.
     */
    public View getRoot()
    {
        return this.mRoot;
    }

    /**
     * @brief Display the context menu for the selected alarm card.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo)
    {
        AppCompatActivity activity = (AppCompatActivity) mContext;
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.alarm_card, menu);
    }

}
