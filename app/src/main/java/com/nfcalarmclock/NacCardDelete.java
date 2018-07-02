package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 * @brief Delete the alarm and its card.
 */
public class NacCardDelete
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
     * @brief Delete button.
     */
     private ImageTextButton mDelete;

    /**
     * @brief Constructor.
     */
    public NacCardDelete(AlarmCard card, Context context)
    {
        this.mContext = context;
        this.mCard = card;
        View root = card.getRoot();
        this.mDelete = (ImageTextButton) root.findViewById(R.id.nacDelete);
        this.mDelete.setOnClickListener(this);
    }

    /**
     * @brief Delete the alarm card.
     */
    @Override
    public void onClick(View v)
    {
        Toast.makeText(mContext, "Deleted alarm.",
                       Toast.LENGTH_SHORT).show();
        mCard.remove();
    }

}
