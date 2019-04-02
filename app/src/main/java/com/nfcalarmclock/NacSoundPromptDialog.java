package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import java.util.List;

/**
 * Save the name of the selected ringtone or song.
 */
public class NacSoundPromptDialog
	extends NacDialog
	implements View.OnClickListener,
		NacDialog.OnDismissListener,
		NacDialog.OnShowListener
{

	/**
	 * Listener for when an item in a dialog is selected.
	 */
	private NacMediaDialog.OnItemClickListener mItemClickListener;

	/**
	 */
	public NacSoundPromptDialog()
	{
		super();

		this.addOnShowListener(this);

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
	public void onClick(View view)
	{
		Context context = view.getContext();
		int id = view.getId();
		NacMediaDialog dialog = null;

		if (id == R.id.dlg_ringtone)
		{
			dialog = new NacRingtoneDialog();
		}
		else if (id == R.id.dlg_music)
		{
			try
			{
				dialog = new NacMusicDialog();
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

		dialog.saveData(this.getData());
		dialog.build(context);
		dialog.setOnItemClickListener(this.mItemClickListener);
		dialog.addOnDismissListener(this);
		dialog.show();
		dialog.scale();
	}

	/**
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		this.dismiss();

		return true;
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		Button music = (Button) root.findViewById(R.id.dlg_music);
		Button ringtone = (Button) root.findViewById(R.id.dlg_ringtone);

		music.setOnClickListener(this);
		ringtone.setOnClickListener(this);
		this.scale(0.9, 0.5, false, true);
	}

	/**
	 * Set the listener for when an item is selected.
	 */
	public void setOnItemClickListener(
		NacMediaDialog.OnItemClickListener listener)
	{
		this.mItemClickListener = listener;
	}

}
