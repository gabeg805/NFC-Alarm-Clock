package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * The media dialog which prompts the user to choose a ringtone or music file.
 */
public abstract class NacMediaDialog
	extends NacDialog
	implements NacDialog.OnDismissListener,
		NacDialog.OnNeutralActionListener,
		NacDialog.OnCancelListener
{

	/**
	 * Build the dialog using a custom layout.
	 */
	public abstract AlertDialog.Builder build(Context context);

	/**
	 * Scale the dialog using custom scaling.
	 */
	public abstract void scale();

	/**
	 * Called when an item is selected.
	 */
	public interface OnItemClickListener
	{
		public void onItemClick(String path, String name);
	}

	/**
	 * Media player.
	 */
	protected NacMediaPlayer mPlayer;

	/**
	 * Path to the media file.
	 */
	protected String mPath;

	/**
	 * Name of the media file.
	 */
	protected String mName;

	/**
	 * On item click listener.
	 */
	protected OnItemClickListener mItemClickListener;

	/**
	 */
	public NacMediaDialog()
	{
		super();

		this.mPlayer = null;
		this.mPath = null;
		this.mName = null;
		this.mItemClickListener = null;

		this.addOnDismissListener(this);
		this.addOnCancelListener(this);
	}

	/**
	 * Item has been selected in the dialog.
	 */
	private void itemClick(String path, String name)
	{
		if (this.mItemClickListener != null)
		{
			this.mItemClickListener.onItemClick(path, name);
		}
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		this.mPlayer = new NacMediaPlayer(context);

		this.setPositiveButton("OK");
		this.setNegativeButton("Cancel");
		this.setNeutralButton("Clear");
	}

	/**
	 * Cancel the dialog.
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	{
		this.mPlayer.release();

		return true;
	}

	/**
	 * Dismiss the dialog.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		this.mPlayer.release();
		this.itemClick(this.mPath, this.mName);

		this.mPath = "";
		this.mName = "";

		return true;
	}

	/**
	 * Clear the selected item.
	 */
	@Override
	public boolean onNeutralActionDialog(NacDialog dialog)
	{
		try
		{
			this.mPlayer.stop();
		}
		catch (IllegalStateException e)
		{
			NacUtility.printf("Caught IllegalStateException in NacMediaDialog onNeutralActionDialog");
		}
		finally
		{
			this.mPlayer.reset();

			this.mPath = "";
			this.mName = "";
		}

		return true;
	}

	/**
	 * Play the sound.
	 */
	public void play(String path, String name)
	{
		this.mPath = path;
		this.mName = name;

		this.mPlayer.play(path);
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
