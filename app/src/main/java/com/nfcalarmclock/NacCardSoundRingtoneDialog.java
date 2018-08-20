package com.nfcalarmclock;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.ArrayList;
import java.util.List;

public class NacCardSoundRingtoneDialog
	extends NacCardDialog
	implements CompoundButton.OnCheckedChangeListener,DialogInterface.OnClickListener
{

	/**
	 * @brief Context.
	 */
	private Context mContext;

	/**
	 * @brief Root view.
	 */
	private View mRoot = null;

	/**
	 * @brief Ringtone manager.
	 */
 	private RingtoneManager mRingtoneManager = null;

	/**
	 * @brief List of ringtones.
	 */
	private List<NacSong> mSounds = null;

	/**
	 * @brief Media player.
	 */
	private NacCardMediaPlayer mPlayer = null;

	/**
	 * @param  c  Context.
	 */
	public NacCardSoundRingtoneDialog(Context c, NacCardMediaPlayer mp)
	{
		super(c);

		this.mContext = c;
		this.mPlayer = mp;
		this.mRingtoneManager = new RingtoneManager(mContext);
		this.mRoot = super.inflate(R.layout.dlg_alarm_sound_ringtone);

		this.mRingtoneManager.setType(RingtoneManager.TYPE_ALARM);
		this.mRingtoneManager.setStopPreviousRingtone(true);
	}

	/**
	 * @brief Show the ringtone dialog.
	 */
	public void show()
	{
		String title = mContext.getString(R.string.dlg_ringtone_title);

		this.init();
		super.build(mRoot, title, this, this);
		super.scale(0.75, 0.75);
	}

	/**
	 * @brief Initialize the ringtone dialog.
	 */
	private void init()
	{
		setSoundList();
		createRadioButtons();
	}

	/**
	 * @brief Set the list of sounds.
	 */
	private void setSoundList()
	{
		this.mSounds = new ArrayList<>();
		Cursor cursor = this.mRingtoneManager.getCursor();
		boolean found = false;

		while (cursor.moveToNext())
		{
			found = false;
			String name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
			String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
			String dir = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)+"/"+id;
			Uri uri = Uri.parse(dir);

			for (NacSong s : this.mSounds)
			{
				if (s.name == name)
				{
					found = true;
					break;
				}
			}

			if (found)
			{
				continue;
			}

			this.mSounds.add(new NacSong(name, dir, uri));
		}
	}

	/**
	 * @brief Create radio buttons from the list of ringtones.
	 */
	private void createRadioButtons()
	{
		RadioGroup rg = (RadioGroup) mRoot.findViewById(R.id.radio_group);

		for(int i=0; i < this.mSounds.size(); i++)
		{
			RadioButton rb = new RadioButton(mContext);
			String name = this.mSounds.get(i).name;

			rb.setText(name);
			rb.setTag(i);
			rb.setOnCheckedChangeListener(this);
			rg.addView(rb);
		}

		// Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(this,
		//                                                             RingtoneManager.TYPE_NOTIFICATION);
		// Alarm alarm = mCard.getAlarm();
		// this.mEditText.setText(alarm.getName());
	}

	/**
	 * @brief Handles how the ringtones interact when selecting different
	 *        radio buttons.
	 */
	@Override
	public void onCheckedChanged(CompoundButton b, boolean state)
	{
		if (!state)
		{
			return;
		}

		int i = (int) b.getTag();
		Uri uri = mSounds.get(i).uri;

		mPlayer.reset();
		mPlayer.run(uri);
	}

	/**
	 * @brief Handles click events on the Ok/Cancel buttons in the dialog.
	 */
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		super.onClick(dialog, which);
		mPlayer.reset();
	}

}
