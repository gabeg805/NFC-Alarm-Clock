package com.nfcalarmclock;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

/**
 * @brief The dialog class that will handle saving the name of the alarm.
 */
public class NacCardSoundPromptDialog
	extends NacCardDialog
	implements View.OnClickListener,DialogInterface.OnDismissListener
{

	/**
	 * @brief Context.
	 */
	private Context mContext = null;

	/**
	 * @brief Dialog.
	 */
	private NacCardDialog mDialog = null;

	/**
	 * @brief Root view.
	 */
	private View mRoot = null;

	/**
	 * @brief Media player.
	 */
	private NacCardMediaPlayer mPlayer = null;

	/**
	 * @param  c  Context.
	 */
	public NacCardSoundPromptDialog(Context c)
	{
		super(c);

		this.mContext = c;
		this.mRoot = super.inflate(R.layout.dlg_alarm_sound_prompt);
		this.mPlayer = new NacCardMediaPlayer(mContext);
		Button music = (Button) mRoot.findViewById(R.id.dlg_music);
		Button ringtone = (Button) mRoot.findViewById(R.id.dlg_ringtone);

		music.setOnClickListener(this);
		ringtone.setOnClickListener(this);
		music.setTag("music");
		ringtone.setTag("ringtone");
	}

	/**
	 * @brief Show the dialog to set the alarm name.
	 */
	public void show()
	{
		String title = mContext.getString(R.string.dlg_prompt_title);

		super.build(mRoot, title, false, true);
	}

	/**
	 * @brief Handle button click events.
	 */
	@Override
	public void onClick(View v)
	{
		String tag = (String) v.getTag();

		if (mDialog != null)
		{
			mDialog.dismiss();
		}

		if (tag == null)
		{
			return;
		}
		else if (tag.equals("music"))
		{
			NacCardSoundMusicDialog d = new NacCardSoundMusicDialog(mContext,
				mPlayer);
			d.show();
			d.setOnDismissListener(this);
			mDialog = d;
		}
		else if (tag.equals("ringtone"))
		{
			NacCardSoundRingtoneDialog d = new NacCardSoundRingtoneDialog(
				mContext, mPlayer);
			d.show();
			d.setOnDismissListener(this);
			mDialog = d;
		}
		else
		{
			return;
		}
	}

	/**
	 * @brief Handles click events on the Ok/Cancel buttons in the dialog.
	 */
	@Override
	public void onDismiss(DialogInterface dialog)
	{
		if (!mDialog.wasCanceled())
		{
			super.dismiss();
		}
	}

}


