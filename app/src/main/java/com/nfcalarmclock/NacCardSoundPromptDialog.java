package com.nfcalarmclock;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import java.util.List;

/**
 * @brief The dialog class that will handle saving the name of the alarm.
 */
public class NacCardSoundPromptDialog
	extends NacCardDialog
	implements View.OnClickListener,DialogInterface.OnDismissListener
{

	/**
	 * @brief Interface for other classes to implement what to do when an
	 *        item is selected.
	 */
	public interface OnItemSelectedListener
	{
		public void onItemSelected(NacSound sound);
	}

	/**
	 * @brief Context.
	 */
	private Context mContext = null;

	/**
	 * @brief Listener for when an item in a dialog is selected.
	 */
	private OnItemSelectedListener mListener = null;

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
	private NacMediaPlayer mPlayer = null;

	/**
	 * @param  c  Context.
	 */
	public NacCardSoundPromptDialog(Context c)
	{
		super(c);

		this.mContext = c;
		this.mRoot = super.inflate(R.layout.dlg_alarm_sound_prompt);
		this.mPlayer = new NacMediaPlayer(mContext);
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
	 * @brief Item has been selected in the dialog.
	 */
	private void itemSelected(NacSound sound)
	{
		if (this.mListener != null)
		{
			mListener.onItemSelected(sound);
		}
	}

	/**
	 * @brief Set the listener for when an item is selected.
	 */
	public void setOnItemSelectedListener(OnItemSelectedListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * @return The sound selection or null if none found.
	 */
	private NacSound getSound()
	{
		List<NacSound> sounds;
		int index = -1;

		if (mDialog instanceof NacCardSoundRingtoneDialog)
		{
			sounds = ((NacCardSoundRingtoneDialog)mDialog).mSounds;
			index = ((NacCardSoundRingtoneDialog)mDialog).mIndex;
		}
		else if (mDialog instanceof NacCardSoundMusicDialog)
		{
			sounds = ((NacCardSoundMusicDialog)mDialog).mSounds;
			index = ((NacCardSoundMusicDialog)mDialog).mIndex;
		}
		else
		{
			return null;
		}

		return (index < 0) ? null : sounds.get(index);
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
		NacSound sound = this.getSound();

		mPlayer.reset();

		if (mDialog.wasCanceled() || (sound == null))
		{
			return;
		}

		itemSelected(sound);
		super.dismiss();
	}

}
