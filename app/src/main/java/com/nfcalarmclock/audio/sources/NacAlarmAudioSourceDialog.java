package com.nfcalarmclock.audio.sources;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;

import java.util.List;


/**
 */
public class NacAlarmAudioSourceDialog
	extends DialogFragment
{

	/**
	 * Listener for when an audio source is selected.
	 */
	public interface OnAudioSourceSelectedListener
	{
		public void onAudioSourceSelected(String audioSource);
	}

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacAlarmAudioSourceDialog";

	/**
	 * Default audio source.
	 */
	private String mDefaultAudioSource;

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

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

		if (listener != null)
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
	 * Get the shared preferences.
	 *
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
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
		return new AlertDialog.Builder(requireContext())
			.setTitle(getString(R.string.title_audio_source))
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

		AlertDialog dialog = (AlertDialog) getDialog();
		Context context = getContext();

		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mRadioGroup = dialog.findViewById(R.id.audio_sources);

		this.setupAudioSources();
		this.setupAudioSourceColor();
		this.setupDialogColor();
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

	/**
	 * Setup the dialog color.
	 */
	private void setupDialogColor()
	{
		AlertDialog dialog = (AlertDialog) getDialog();
		Button okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		Button cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		int themeColor = this.getSharedPreferences().getThemeColor();

		okButton.setTextColor(themeColor);
		cancelButton.setTextColor(themeColor);
	}

}

