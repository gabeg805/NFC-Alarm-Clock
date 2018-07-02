package com.nfcalarmclock;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;

/**
 * @brief Expand the alarm card.
 * 
 * @details When the alarm card is not at the top of the screen, clicking the
 *          expand button will scroll the screen so that the alarm card is at
 *          the top.
 */
public class NacCardExpand
    implements View.OnClickListener
{

    /**
     * @brief Activity.
     */
    private AppCompatActivity mActivity;

    /**
     * @brief Context.
     */
     private Context mContext;

    /**
     * @brief Alarm card.
     */
     private AlarmCard mCard;

    /**
     * @brief Expand button.
     */
     private ImageView mExpand;

    /**
     * @brief Constructor.
     */
    public NacCardExpand(AlarmCard card, Context context)
    {
        this.mActivity = (AppCompatActivity) context;
        this.mContext = context;
        this.mCard = card;
        View root = card.getRoot();
        this.mExpand = (ImageView) root.findViewById(R.id.nacExpand);
        this.mExpand.setOnClickListener(this);
    }

    /**
     * @brief Expand the alarm card when the button is clicked.
     */
    @Override
    public void onClick(View v)
    {
        int pos = ((ViewHolder)mCard).getAdapterPosition();
        RecyclerView.SmoothScroller scroller = getSmoothScroller(pos);
        RecyclerView rv = (RecyclerView)mActivity.findViewById(R.id.content_alarm_list);
        rv.getLayoutManager().startSmoothScroll(scroller);
        mCard.expand();
    }

    /**
     * @brief Create a SmoothScroller to scroll to the specified position.
     * 
     * @param pos  The adapter position to scroll to.
     */
    public RecyclerView.SmoothScroller getSmoothScroller(int pos)
    {
        RecyclerView.SmoothScroller scroller =
            new LinearSmoothScroller(mContext)
            {
                @Override
                protected int getVerticalSnapPreference()
                {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
        scroller.setTargetPosition(pos);
        return scroller;
    }

}
