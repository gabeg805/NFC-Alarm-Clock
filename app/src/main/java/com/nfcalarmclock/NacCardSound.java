package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
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
        this.mSound = (ImageTextButton) r.findViewById(R.id.nacSound);

        this.mSound.setOnClickListener(this);
    }

    /**
     * @brief Initialize the sound view.
     */
    public void init(Alarm alarm)
    {
		this.mAlarm = alarm;
		String path = this.mAlarm.getSound();

		if (path.isEmpty())
		{
			return;
		}

		Uri uri = Uri.parse(path);
		Ringtone ringtone = RingtoneManager.getRingtone(mContext, uri);
		String name = ringtone.getTitle(mContext);

		ringtone.stop();
		this.mSound.setText(name);
    }

    /**
     * @brief Display the dialog that allows users to set the name of thee alarm.
     */
    @Override
    public void onClick(View v)
    {
        this.mPromptDialog = new NacCardSoundPromptDialog(mContext, mSound,
			mAlarm);

        this.mPromptDialog.show();
    }

}
