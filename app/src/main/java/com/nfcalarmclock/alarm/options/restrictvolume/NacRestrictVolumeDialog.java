package com.nfcalarmclock.alarm.options.restrictvolume;

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
import com.nfcalarmclock.view.dialog.NacDialogFragment;

/**
 */
public class NacRestrictVolumeDialog
	extends NacDialogFragment
	implements View.OnClickListener
{

	/**
	 * Listener for when an audio source is selected.
	 */
	@SuppressWarnings("UnnecessaryInterfaceModifier")
	public interface OnRestrictVolumeListener
	{
		public void onRestrictVolume(boolean shouldRestrict);
	}

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacRestrictVolumeDialog";

	/**
	 * Default volume restriction.
	 */
	private boolean mDefaultShouldRestrictVolume;

	/**
	 * Check box to restrict/unrestrict the volume.
	 */
	private MaterialCheckBox mShouldRestrictVolumeCheckBox;

	/**
	 * Summary text for whether volume should be restricted or not.
	 */
	private TextView mShouldRestrictVolumeSummary;

	/**
	 * Listener for when the volume is restricted/unrestricted.
	 */
	private OnRestrictVolumeListener mOnRestrictVolumeListener;

	/**
	 * Call the OnRestrictVolumeListener object, if it has been set.
	 */
	public void callOnRestrictVolumeListener()
	{
		OnRestrictVolumeListener listener = this.getOnRestrictVolumeListener();
		MaterialCheckBox checkBox = this.getShouldRestrictVolumeCheckBox();

		// Call the listener
		if ((listener != null) && (checkBox != null))
		{
			boolean isChecked = checkBox.isChecked();

			listener.onRestrictVolume(isChecked);
		}
	}

	/**
	 * Get the default volume restriction.
	 *
	 * @return The default volume restriction.
	 */
	public boolean getDefaultShouldRestrictVolume()
	{
		return this.mDefaultShouldRestrictVolume;
	}

	/**
	 * Get the OnRestrictVolumeListener object.
	 *
	 * @return The OnRestrictVolumeListener object.
	 */
	public OnRestrictVolumeListener getOnRestrictVolumeListener()
	{
		return this.mOnRestrictVolumeListener;
	}

	/**
	 * Get the restrict volume check box.
	 *
	 * @return The restrict volume check box.
	 */
	public MaterialCheckBox getShouldRestrictVolumeCheckBox()
	{
		return this.mShouldRestrictVolumeCheckBox;
	}

	/**
	 * Get the summary text for whether volume should be restricted or not.
	 *
	 * @return The summary text for whether volume should be restricted or not.
	 */
	private TextView getShouldRestrictVolumeSummary()
	{
		return this.mShouldRestrictVolumeSummary;
	}

	/**
	 * Called when the check box is clicked.
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();

		if (id == R.id.should_restrict_volume)
		{
			this.toggleShouldRestrictVolume();
			this.setupShouldRestrictVolumeSummary();
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
			.setTitle(cons.getTitleRestrictVolume())
			.setPositiveButton(cons.getActionOk(), (dialog, which) ->
				this.callOnRestrictVolumeListener())
			.setNegativeButton(cons.getActionCancel(), (dialog, which) -> {})
			.setView(R.layout.dlg_alarm_restrict_volume)
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
		RelativeLayout container = dialog.findViewById(R.id.should_restrict_volume);

		// Set the member variable widgets
		this.mShouldRestrictVolumeCheckBox =
			dialog.findViewById(R.id.should_restrict_volume_checkbox);
		this.mShouldRestrictVolumeSummary =
			dialog.findViewById(R.id.should_restrict_volume_summary);

		container.setOnClickListener(this);
		this.setupShouldRestrictVolume();
		this.setupShouldRestrictVolumeColor();
	}

	/**
	 * Set the default volume restriction.
	 *
	 * @param  shouldRestrict  The default volume restriction.
	 */
	public void setDefaultShouldRestrictVolume(boolean shouldRestrict)
	{
		this.mDefaultShouldRestrictVolume = shouldRestrict;
	}

	/**
	 * Set the OnRestrictVolumeListener object.
	 *
	 * @param  listener  The OnRestrictVolumeListener object.
	 */
	public void setOnRestrictVolumeListener(
		OnRestrictVolumeListener listener)
	{
		this.mOnRestrictVolumeListener = listener;
	}

	/**
	 * Setup the check box and summary text for whether volume should be
	 * restricted or not.
	 */
	private void setupShouldRestrictVolume()
	{
		boolean shouldRestrict = this.getDefaultShouldRestrictVolume();

		this.getShouldRestrictVolumeCheckBox().setChecked(shouldRestrict);
		this.setupShouldRestrictVolumeSummary();
	}

	/**
	 * Setup the color of the restrict volume check box.
	 */
	private void setupShouldRestrictVolumeColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		int[] colors = new int[] { shared.getThemeColor(), Color.GRAY };
		int[][] states = new int[][] {
			new int[] {  android.R.attr.state_checked },
			new int[] { -android.R.attr.state_checked } };
		ColorStateList colorStateList = new ColorStateList(states, colors);

		this.getShouldRestrictVolumeCheckBox().setButtonTintList(colorStateList);
	}

	/**
	 * Setup the summary text for whether volume should be restricted or not.
	 */
	private void setupShouldRestrictVolumeSummary()
	{
		boolean shouldRestrict = this.shouldRestrictVolume();
		int textId = shouldRestrict ? R.string.restrict_volume_true
			: R.string.restrict_volume_false;

		this.getShouldRestrictVolumeSummary().setText(textId);
	}

	/**
	 * Get whether volume should be restricted or not.
	 *
	 * @return True if volume should be restricted, and False otherwise.
	 */
	private boolean shouldRestrictVolume()
	{
		return this.getShouldRestrictVolumeCheckBox().isChecked();
	}

	/**
	 * Toggle whether volume should be restricted or not.
	 */
	private void toggleShouldRestrictVolume()
	{
		boolean shouldRestrict = this.shouldRestrictVolume();

		this.getShouldRestrictVolumeCheckBox().setChecked(!shouldRestrict);
	}

}

