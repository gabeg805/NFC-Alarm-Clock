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
 * The sound to play when the alarm is activated. Users can change this by
 * selecting the sound view.
 */
public class NacCardSound
	implements View.OnClickListener,NacSoundDialog.OnItemClickListener
{

	/**
	 * Alarm.
	 */
	 private Alarm mAlarm;

	/**
	 * Sound.
	 */
	 private ImageTextButton mSoundView;

	/**
	 */
	public NacCardSound(Context c, View r)
	{
		this.mSoundView = (ImageTextButton) r.findViewById(R.id.nacSound);
		this.mAlarm = null;

		this.mSoundView.setOnClickListener(this);
	}

	/**
	 * Initialize the sound view.
	 */
	public void init(Alarm alarm)
	{
		String path = alarm.getSound();
		this.mAlarm = alarm;

		if (path.isEmpty())
		{
			return;
		}

		Context context = this.mSoundView.getContext();
		Uri uri = Uri.parse(path);
		Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
		String name = ringtone.getTitle(context);

		ringtone.stop();
		this.mSoundView.setText(name);
	}

	/**
	 * Display the dialog that allows users to set the name of the alarm.
	 */
	@Override
	public void onClick(View v)
	{
		Context context = v.getContext();
		NacSoundPromptDialog dialog = new NacSoundPromptDialog();

		dialog.build(context, R.layout.dlg_sound_prompt);
		dialog.setOnItemClickListener(this);
		dialog.show();
	}

	/**
	 * @brief Handle the sound item when it has been selected.
	 */
	@Override
	public void onItemClick(NacSound sound)
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
