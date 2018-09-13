package com.nfcalarmclock;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
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
	private Context mContext = null;

	/**
	 * @brief The sound view in the alarm card.
	 */
	private ImageTextButton mSoundView = null;

	/**
	 * @brief Media player.
	 */
	private NacMediaPlayer mPlayer = null;

	/**
	 * @brief Ringtone manager.
	 */
 	private RingtoneManager mRingtoneManager = null;

	/**
	 * @brief Root view.
	 */
	private View mRoot = null;

	/**
	 * @brief List of ringtones.
	 */
	public List<NacSong> mSounds = null;

	/**
	 * @brief The index in the songs list pointing to the currently selected
	 * item.
	 */
	public int mIndex = -1;

	/**
	 * @param  c  Context.
	 */
	public NacCardSoundRingtoneDialog(Context c, ImageTextButton b,
		NacMediaPlayer mp)
	{
		super(c);

		this.mContext = c;
		this.mSoundView = b;
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
			String dir = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);

			NacUtility.printf("File : %s/%s (%s)", dir, id, name);

			for (NacSong s : this.mSounds)
			{
				if (s.ringtone == name)
				{
					found = true;
					break;
				}
			}

			if (found)
			{
				continue;
			}

			this.mSounds.add(new NacSong(id, dir, name));
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
			String name = this.mSounds.get(i).ringtone;

			rb.setText(name);
			rb.setTag(i);
			rb.setOnCheckedChangeListener(this);
			rg.addView(rb);
		}
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
		String path = this.mSounds.get(i).path;
		this.mIndex = i;

		this.mPlayer.play(path);
	}

	/**
	 * @brief Handles click events on the Ok/Cancel buttons in the dialog.
	 */
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		super.onClick(dialog, which);
	}

}
