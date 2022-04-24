package com.nfcalarmclock.audiosource;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.util.dialog.NacDialogFragment;

import java.util.List;


/**
 */
public class NacAudioSourceDialog
	extends NacDialogFragment
{

	/**
	 * Listener for when an audio source is selected.
	 */
	@SuppressWarnings("UnnecessaryInterfaceModifier")
	public interface OnAudioSourceSelectedListener
	{
		public void onAudioSourceSelected(String audioSource);
	}

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacAudioSourceDialog";

	/**
	 * Default audio source.
	 */
	private String mDefaultAudioSource;

	/**
	 * Radio button group for each alarm source.
	 */
	private RadioGroup mRadioGroup;

	/**
	 * Listener for when an audio source is selected.
	 */
	private OnAudioSourceSelectedListener mOnAudioSourceSelectedListener;

	/**
	 * Call the OnAudioSourceSelectedListener object, if it has been set.
	 */
	public void callOnAudioSourceSelectedListener()
	{
		OnAudioSourceSelectedListener listener =
			this.getOnAudioSourceSelectedListener();
		RadioGroup group = this.getRadioGroup();

		if ((listener != null) && (group != null))
		{
			String audioSource = this.getAudioSource();

			listener.onAudioSourceSelected(audioSource);
		}
	}

	/**
	 * Get the list of all audio sources.
	 *
	 * @return The list of all audio sources.
	 */
	private List<String> getAllAudioSources()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacSharedConstants cons = shared.getConstants();

		return cons.getAudioSources();
	}

	/**
	 * Get the selected audio source.
	 *
	 * @return The selected audio source.
	 */
	public String getAudioSource()
	{
		RadioGroup group = this.getRadioGroup();
		int checkedId = group.getCheckedRadioButtonId();
		RadioButton button = group.findViewById(checkedId);

		return button.getText().toString();
	}

	/**
	 * Get the default audio source.
	 *
	 * @return The default audio source.
	 */
	public String getDefaultAudioSource()
	{
		return this.mDefaultAudioSource;
	}

	/**
	 * Get the radio group for the audio sources.
	 *
	 * @return The audio sources radio group.
	 */
	public RadioGroup getRadioGroup()
	{
		return this.mRadioGroup;
	}

	/**
	 * Get the OnAudioSourceSelectedListener object.
	 *
	 * @return The OnAudioSourceSelectedListener object.
	 */
	public OnAudioSourceSelectedListener getOnAudioSourceSelectedListener()
	{
		return this.mOnAudioSourceSelectedListener;
	}

	/**
	 */
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
	{
		this.setupSharedPreferences();

		NacSharedConstants cons = this.getSharedConstants();

		return new AlertDialog.Builder(requireContext())
			.setTitle(cons.getTitleAudioSource())
			//.setSingleChoiceItems(R.array.audio_sources, -1, (dialog, which) -> {})
			.setPositiveButton(getString(R.string.action_ok), (dialog, which) ->
				this.callOnAudioSourceSelectedListener())
			.setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> {})
			.setView(R.layout.dlg_alarm_audio_source)
			.create();
	}

	/**
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		// Initialize the widgets
		AlertDialog dialog = (AlertDialog) getDialog();
		this.mRadioGroup = dialog.findViewById(R.id.audio_sources);

		// Setup the dialog and widgets
		this.setupAudioSources();
		this.setupAudioSourceColor();
	}

	/**
	 * Set the default audio source.
	 *
	 * @param  audioSource  The default audio source.
	 */
	public void setDefaultAudioSource(String audioSource)
	{
		this.mDefaultAudioSource = audioSource;
	}

	/**
	 * Set the OnAudioSourceSelectedListener object.
	 *
	 * @param  listener  The OnAudioSourceSelectedListener object.
	 */
	public void setOnAudioSourceSelectedListener(
		OnAudioSourceSelectedListener listener)
	{
		this.mOnAudioSourceSelectedListener = listener;
	}

	/**
	 * Setup the audio source.
	 */
	private void setupAudioSources()
	{
		RadioGroup group = this.getRadioGroup();
		int count = group.getChildCount();

		if (count > 0)
		{
			return;
		}

		LayoutInflater inflater = getLayoutInflater();
		String defSource = this.getDefaultAudioSource();

		for (String s : this.getAllAudioSources())
		{
			View view = inflater.inflate(R.layout.radio_button, group, true);
			RadioButton button = view.findViewById(R.id.radio_button);
			int id = View.generateViewId();

			button.setId(id);
			button.setText(s);

			if (!defSource.isEmpty() && s.equals(defSource))
			{
				button.setChecked(true);
			}
		}
	}

	/**
	 * Setup the color of the audio source items.
	 */
	private void setupAudioSourceColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		RadioGroup group = this.getRadioGroup();

		int[] colors = new int[] { shared.getThemeColor(), Color.GRAY };
		int[][] states = new int[][] {
			new int[] {  android.R.attr.state_checked },
			new int[] { -android.R.attr.state_checked } };
		ColorStateList colorStateList = new ColorStateList(states, colors);

		for (int i=0; i < group.getChildCount(); i++)
		{
			RadioButton button = (RadioButton) group.getChildAt(i);
			button.setButtonTintList(colorStateList);
		}
	}

}

