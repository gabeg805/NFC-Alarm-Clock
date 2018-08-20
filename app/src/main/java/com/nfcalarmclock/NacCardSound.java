package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief The sound to play when the alarm is activated. Users can change this
 *        by selecting the sound view.
 */
public class NacCardSound
    implements View.OnClickListener
{

    /**
     * @brief Context.
     */
     private Context mContext = null;

    /**
     * @brief Alarm.
     */
     private Alarm mAlarm = null;

    /**
     * @brief Sound.
     */
     private ImageTextButton mSound = null;

    /**
     * @brief Sound dialog.
     */
     private NacCardSoundPromptDialog mPromptDialog = null;

    /**
     * @brief Constructor.
     */
    public NacCardSound(Context c, View r)
    {
        this.mContext = c;
        this.mPromptDialog = new NacCardSoundPromptDialog(mContext);
        this.mSound = (ImageTextButton) r.findViewById(R.id.nacSound);
        this.mSound.setOnClickListener(this);
    }

    /**
     * @brief Initialize the sound view.
     */
    public void init(Alarm alarm)
    {
		this.mAlarm = alarm;
        // this.mSound.setText(this.mCard.getAlarm().getSound());
    }

    /**
     * @brief Display the dialog that allows users to set the name of thee alarm.
     */
    @Override
    public void onClick(View v)
    {
        this.mPromptDialog.show();
    }

}

//listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

//	@Override
//	public void onItemClick(AdapterView<?> parent, final View view,
//		int position, long id)
//	{
//		//final String item = (String) parent.getItemAtPosition(position);
//		view.animate().setDuration(2000).alpha(0)
//				.withEndAction(new Runnable() {
//					@Override
//					public void run() {
//						//list.remove(item);
//						//adapter.notifyDataSetChanged();
//						view.setAlpha(1);
//					}
//				});
//	}

//});
