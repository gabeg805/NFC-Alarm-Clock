package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
    implements View.OnClickListener,NacCardSoundPromptDialog.OnItemSelectedListener
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
     private ImageTextButton mSoundView = null;

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
        this.mSoundView = (ImageTextButton) r.findViewById(R.id.nacSound);

        this.mSoundView.setOnClickListener(this);
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
		this.mSoundView.setText(name);
    }

    /**
     * @brief Display the dialog that allows users to set the name of the
	 *        alarm.
     */
    @Override
    public void onClick(View v)
    {
        this.mPromptDialog = new NacCardSoundPromptDialog(mContext);

		this.mPromptDialog.setOnItemSelectedListener(this);
        this.mPromptDialog.show();
    }

	/**
	 * @brief Handle the sound item when it has been selected.
	 */
	@Override
	public void onItemSelected(NacSound sound)
	{
		String path = sound.path;
		String name = sound.name;

		if (path.isEmpty() || name.isEmpty())
		{
			return;
		}

		NacUtility.printf("Sound : %s", path);
		this.mSoundView.setText(name);
		this.mAlarm.setSound(path);
		this.mAlarm.changed();
	}

}
