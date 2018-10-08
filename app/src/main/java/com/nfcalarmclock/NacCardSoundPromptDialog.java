package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import java.util.List;

/**
 * @brief The dialog class that will handle saving the name of the alarm.
 */
public class NacCardSoundPromptDialog
	extends NacDialog
	implements View.OnClickListener,NacDialog.OnDismissedListener,NacDialog.OnCanceledListener
{

	/**
	 * Interface for other classes to implement what to do when an item is
	 * selected.
	 */
	public interface OnItemSelectedListener
	{
		public void onItemSelected(NacSound sound);
	}

	/**
	 * Listener for when an item in a dialog is selected.
	 */
	private OnItemSelectedListener mListener;

	/**
	 * Media player.
	 */
	private NacMediaPlayer mPlayer;

	/**
	 */
	public NacCardSoundPromptDialog()
	{
		super();

		this.mListener = null;
		this.mPlayer = null;
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		String title = context.getString(R.string.dlg_prompt_title);
		this.mPlayer = new NacMediaPlayer(context);

		builder.setTitle(title);
		this.setNegativeButton("Cancel");
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(Context context, View root)
	{
		Button music = (Button) root.findViewById(R.id.dlg_music);
		Button ringtone = (Button) root.findViewById(R.id.dlg_ringtone);

		music.setOnClickListener(this);
		ringtone.setOnClickListener(this);
		music.setTag("music");
		ringtone.setTag("ringtone");
	}

	/**
	 * Item has been selected in the dialog.
	 */
	private void itemSelected(NacSound sound)
	{
		if (this.mListener != null)
		{
			mListener.onItemSelected(sound);
		}
	}

	/**
	 * Set the listener for when an item is selected.
	 */
	public void setOnItemSelectedListener(OnItemSelectedListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * @return The sound selection or null if none found.
	 */
	private NacSound getSound(NacDialog dialog)
	{
		NacSound sound = null;

		if (dialog instanceof NacCardSoundRingtoneDialog)
		{
			sound = ((NacCardSoundRingtoneDialog)dialog).getSound();
		}
		else if (dialog instanceof NacCardSoundMusicDialog)
		{
			sound = ((NacCardSoundMusicDialog)dialog).getSound();
		}

		return sound;
	}

	/**
	 * @brief Handle button click events.
	 */
	@Override
	public void onClick(View v)
	{
		Context context = v.getContext();
		String tag = (String) v.getTag();
		NacDialog dialog = null;
		int layout = R.layout.dlg_sound_list;
		double widthscale = 0;
		double heightscale = 0.75;
		boolean wrapwidth = false;
		boolean wrapheight = true;

		if (tag == null)
		{
			return;
		}
		else if (tag.equals("ringtone"))
		{
			dialog = new NacCardSoundRingtoneDialog(mPlayer);
			widthscale = 0.8;
		}
		else if (tag.equals("music"))
		{
			try
			{
				dialog = new NacCardSoundMusicDialog(mPlayer);
				widthscale = 0.9;
			}
			catch (UnsupportedOperationException e)
			{
				NacUtility.printf("Caught the exception. Not going to show the dialog.");
				return;
			}
		}
		else
		{
			return;
		}

		dialog.build(context, layout);
		dialog.addDismissListener(this);
		dialog.addCancelListener(this);
		dialog.show();
		dialog.scale(widthscale, heightscale, wrapwidth, wrapheight);
	}

	/**
	 */
	@Override
	public void onDialogCanceled(NacDialog dialog)
	{
		this.mPlayer.reset();
		this.cancel();
	}

	/**
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		NacSound sound = this.getSound(dialog);

		this.mPlayer.reset();
		this.dismiss();

		if (sound != null)
		{
			this.itemSelected(sound);
		}
	}

}
