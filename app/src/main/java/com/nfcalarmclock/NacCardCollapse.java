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
 * @brief Collapse the alarm card.
 */
public class NacCardCollapse
    implements View.OnClickListener
{

    /**
     * @brief Context.
     */
     private Context mContext;

    /**
     * @brief Alarm card.
     */
     private AlarmCard mCard;

    /**
     * @brief Collapse button.
     */
     private ImageView mCollapse;

    /**
     * @brief Constructor.
     */
    public NacCardCollapse(AlarmCard card, Context context)
    {
        this.mContext = context;
        this.mCard = card;
        View root = card.getRoot();
        this.mCollapse = (ImageView) root.findViewById(R.id.nacCollapse);
        this.mCollapse.setOnClickListener(this);
    }

    /**
     * @brief Collapse the alarm card when the button is clicked.
     */
    @Override
    public void onClick(View v)
    {
        mCard.collapse();
    }

}
