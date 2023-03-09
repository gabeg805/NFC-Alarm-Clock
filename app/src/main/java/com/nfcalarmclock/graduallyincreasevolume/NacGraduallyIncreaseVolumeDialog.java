package com.nfcalarmclock.graduallyincreasevolume;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.checkbox.MaterialCheckBox;

import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.util.dialog.NacDialogFragment;

/**
 */
public class NacGraduallyIncreaseVolumeDialog
	extends NacDialogFragment
	implements View.OnClickListener
{

	/**
	 * Listener for when an audio source is selected.
	 */
	@SuppressWarnings("UnnecessaryInterfaceModifier")
	public interface OnGraduallyIncreaseVolumeListener
	{
		public void onGraduallyIncreaseVolume(boolean shouldIncrease);
	}

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacGraduallyIncreaseVolumeDialog";

	/**
	 * Default value for whether the volume should be gradually increased.
	 */
	private boolean mDefaultShouldGraduallyIncreaseVolume;

	/**
	 * Check box for whether the volume should be gradually increased or not.
	 */
	private MaterialCheckBox mShouldGraduallyIncreaseVolumeCheckBox;

	/**
	 * Summary text for whether volume should be gradually increase or not.
	 */
	private TextView mShouldGraduallyIncreaseVolumeSummary;

	/**
	 * Listener for when the volume is gradually increased or not.
	 */
	private OnGraduallyIncreaseVolumeListener mOnGraduallyIncreaseVolumeListener;

	/**
	 * Call the OnGraduallyIncreaseVolumeListener object, if it has been set.
	 */
	public void callOnGraduallyIncreaseVolumeListener()
	{
		OnGraduallyIncreaseVolumeListener listener = this.getOnGraduallyIncreaseVolumeListener();
		MaterialCheckBox checkBox = this.getShouldGraduallyIncreaseVolumeCheckBox();

		// Call the listener
		if ((listener != null) && (checkBox != null))
		{
			boolean isChecked = checkBox.isChecked();

			listener.onGraduallyIncreaseVolume(isChecked);
		}
	}

	/**
	 * Default value for whether the volume should be gradually increased.
	 *
	 * @return The default gradually increase volume value.
	 */
	public boolean getDefaultShouldGraduallyIncreaseVolume()
	{
		return this.mDefaultShouldGraduallyIncreaseVolume;
	}

	/**
	 * Get the OnGraduallyIncreaseVolumeListener object.
	 *
	 * @return The OnGraduallyIncreaseVolumeListener object.
	 */
	public OnGraduallyIncreaseVolumeListener getOnGraduallyIncreaseVolumeListener()
	{
		return this.mOnGraduallyIncreaseVolumeListener;
	}

	/**
	 * Get the gradually increase volume check box.
	 *
	 * @return The gradually increase volume check box.
	 */
	public MaterialCheckBox getShouldGraduallyIncreaseVolumeCheckBox()
	{
		return this.mShouldGraduallyIncreaseVolumeCheckBox;
	}

	/**
	 * Get the summary text for whether volume should be gradually increased or not.
	 *
	 * @return The summary text for whether volume should be gradually increased or not.
	 */
	private TextView getShouldGraduallyIncreaseVolumeSummary()
	{
		return this.mShouldGraduallyIncreaseVolumeSummary;
	}

	/**
	 * Called when the check box is clicked.
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();

		if (id == R.id.should_gradually_increase_volume)
		{
			this.toggleShouldGraduallyIncreaseVolume();
			this.setupShouldGraduallyIncreaseVolumeSummary();
		}
	}

	/**
	 * Called when the dialog is created.
	 */
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
	{
		this.setupSharedPreferences();

		NacSharedConstants cons = this.getSharedConstants();

		return new AlertDialog.Builder(requireContext())
			.setTitle(cons.getTitleGraduallyIncreaseVolume())
			.setPositiveButton(cons.getActionOk(), (dialog, which) ->
				this.callOnGraduallyIncreaseVolumeListener())
			.setNegativeButton(cons.getActionCancel(), (dialog, which) -> {})
			.setView(R.layout.dlg_alarm_gradually_increase_volume)
			.create();
	}

	/**
	 * Called when the fragment is resumed.
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		AlertDialog dialog = (AlertDialog) getDialog();
		RelativeLayout container = dialog.findViewById(R.id.should_gradually_increase_volume);

		// Set the member variable widgets
		this.mShouldGraduallyIncreaseVolumeCheckBox =
			dialog.findViewById(R.id.should_gradually_increase_volume_checkbox);
		this.mShouldGraduallyIncreaseVolumeSummary =
			dialog.findViewById(R.id.should_gradually_increase_volume_summary);

		container.setOnClickListener(this);
		this.setupShouldGraduallyIncreaseVolume();
		this.setupShouldGraduallyIncreaseVolumeColor();
	}

	/**
	 * Set the default gradually increase volume value.
	 *
	 * @param  shouldIncrease  The default gradually increase volume value.
	 */
	public void setDefaultShouldGraduallyIncreaseVolume(boolean shouldIncrease)
	{
		this.mDefaultShouldGraduallyIncreaseVolume = shouldIncrease;
	}

	/**
	 * Set the OnGraduallyIncreaseVolumeListener object.
	 *
	 * @param  listener  The OnGraduallyIncreaseVolumeListener object.
	 */
	public void setOnGraduallyIncreaseVolumeListener(
		OnGraduallyIncreaseVolumeListener listener)
	{
		this.mOnGraduallyIncreaseVolumeListener = listener;
	}

	/**
	 * Setup the check box and summary text for whether volume should be
	 * gradually increased or not.
	 */
	private void setupShouldGraduallyIncreaseVolume()
	{
		boolean shouldIncrease = this.getDefaultShouldGraduallyIncreaseVolume();

		this.getShouldGraduallyIncreaseVolumeCheckBox().setChecked(shouldIncrease);
		this.setupShouldGraduallyIncreaseVolumeSummary();
	}

	/**
	 * Setup the color of the gradually increase volume check box.
	 */
	private void setupShouldGraduallyIncreaseVolumeColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		int[] colors = new int[] { shared.getThemeColor(), Color.GRAY };
		int[][] states = new int[][] {
			new int[] {  android.R.attr.state_checked },
			new int[] { -android.R.attr.state_checked } };
		ColorStateList colorStateList = new ColorStateList(states, colors);

		this.getShouldGraduallyIncreaseVolumeCheckBox().setButtonTintList(colorStateList);
	}

	/**
	 * Setup the summary text for whether volume should be gradually increased or not.
	 */
	private void setupShouldGraduallyIncreaseVolumeSummary()
	{
		boolean shouldIncrease = this.shouldGraduallyIncreaseVolume();
		int textId = shouldIncrease ? R.string.gradually_increase_volume_true
			: R.string.gradually_increase_volume_false;

		this.getShouldGraduallyIncreaseVolumeSummary().setText(textId);
	}

	/**
	 * Get whether volume should be gradually increased or not.
	 *
	 * @return True if volume should be gradually increased, and False otherwise.
	 */
	private boolean shouldGraduallyIncreaseVolume()
	{
		return this.getShouldGraduallyIncreaseVolumeCheckBox().isChecked();
	}

	/**
	 * Toggle whether volume should be gradually increased or not.
	 */
	private void toggleShouldGraduallyIncreaseVolume()
	{
		boolean shouldIncrease = this.shouldGraduallyIncreaseVolume();

		this.getShouldGraduallyIncreaseVolumeCheckBox().setChecked(!shouldIncrease);
	}

}

