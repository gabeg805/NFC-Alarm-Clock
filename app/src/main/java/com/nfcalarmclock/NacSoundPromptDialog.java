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
public class NacSoundPromptDialog
	extends NacDialog
	implements View.OnClickListener,NacDialog.OnDismissedListener
{

	/**
	 * Listener for when an item in a dialog is selected.
	 */
	private NacSoundDialog.OnItemClickListener mItemClickListener;

	/**
	 */
	public NacSoundPromptDialog()
	{
		super();

		this.mItemClickListener = null;
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		String title = context.getString(R.string.dlg_prompt_title);

		builder.setTitle(title);
		this.setNegativeButton("Cancel");
	}

	/**
	 * Handle button click events.
	 */
	@Override
	public void onClick(View v)
	{
		Context context = v.getContext();
		int layout = R.layout.dlg_sound_list;
		double widthscale = 0;
		double heightscale = 0.75;
		boolean wrapwidth = false;
		boolean wrapheight = true;
		NacSoundDialog dialog = null;
		int id = v.getId();

		if (id == R.id.dlg_ringtone)
		{
			dialog = new NacSoundRingtoneDialog();
			widthscale = 0.8;
		}
		else if (id == R.id.dlg_music)
		{
			try
			{
				dialog = new NacSoundMusicDialog();
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
		dialog.setOnItemClickListener(this.mItemClickListener);
		dialog.addDismissListener(this);
		dialog.show();
		dialog.scale(widthscale, heightscale, wrapwidth, wrapheight);
	}

	/**
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		NacUtility.printf("Dialog dismissed.");
		this.dismiss();
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
	}

	/**
	 * Set the listener for when an item is selected.
	 */
	public void setOnItemClickListener(
		NacSoundDialog.OnItemClickListener listener)
	{
		this.mItemClickListener = listener;
	}

}
