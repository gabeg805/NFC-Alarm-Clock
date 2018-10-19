package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
	 * Sound.
	 */
	 private ImageTextButton mSoundView;

	/**
	 * Alarm.
	 */
	 private Alarm mAlarm;

	/**
	 */
	public NacCardSound(View root)
	{
		this.mSoundView = (ImageTextButton) root.findViewById(R.id.nacSound);
		this.mAlarm = null;

		this.mSoundView.setOnClickListener(this);
	}

	/**
	 * Initialize the sound view.
	 */
	public void init(Alarm alarm)
	{
		this.mAlarm = alarm;

		this.setSound();
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
	 * Handle the sound item when it has been selected.
	 */
	@Override
	public void onItemClick(NacSound sound)
	{
		String path = sound.path;
		//String name = sound.name;

		//if (path.isEmpty() || name.isEmpty())
		//{
		//	return;
		//}

		//this.mSoundView.setText(name);
		this.mAlarm.setSound(path);
		this.setSound();
		this.mAlarm.changed();
	}

	/**
	 * Set the sound.
	 */
	public void setSound()
	{
		TextView tv = this.mSoundView.getTextView();
		String path = this.mAlarm.getSound();
		String name = Alarm.getSoundNameDefault();
		float alpha = 0.5f;
		int face = Typeface.ITALIC;

		NacUtility.printf("Parsing path : %s", path);

		if (!path.isEmpty())
		{
			Context context = this.mSoundView.getContext();
			name = this.mAlarm.getSoundName(context);
			alpha = 1.0f;
			face = Typeface.NORMAL;
			NacUtility.printf("Opacity set to 1.0");
		}
		else
		{
			NacUtility.printf("Opacity set to 0.5");
		}

		NacUtility.printf("Parsed name : %s", name);
		NacUtility.printf("Sound : %s", path);
		tv.setAlpha(alpha);
		tv.setTypeface(Typeface.defaultFromStyle(face));
		this.mSoundView.setText(name);
	}

}
