package com.nfcalarmclock.tts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.checkbox.MaterialCheckBox;

import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.util.dialog.NacDialogFragment;

import java.util.List;

/**
 */
public class NacTextToSpeechDialog
	extends NacDialogFragment
	implements View.OnClickListener
{

	/**
	 * Listener for when text-to-speech options are selected.
	 */
	@SuppressWarnings("UnnecessaryInterfaceModifier")
	public interface OnTextToSpeechOptionsSelectedListener
	{
		public void onTextToSpeechOptionsSelected(boolean useTts, int freq);
	}

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacAlarmTextToSpeechDialog";

	/**
	 * Default flag indicating whether text-to-speech should be used or not.
	 */
	private boolean mDefaultUseTts;

	/**
	 * Default text-to-speech frequency.
	 */
	private int mDefaultTtsFrequency;

	/**
	 * Checkbox for setting whether text-to-speech should be used or not.
	 */
	private MaterialCheckBox mShouldUseTtsCheckBox;

	/**
	 * Summary text for whether text-to-speech should be used or not.
	 */
	private TextView mShouldUseTtsSummary;

	/**
	 * Scrollable picker to choose the text-to-speech frequency.
	 */
	private NumberPicker mTtsFrequencyPicker;

	/**
	 * Listener for when a text-to-speech option and frequency is selected.
	 */
	private OnTextToSpeechOptionsSelectedListener mOnTextToSpeechOptionsSelectedListener;

	/**
	 * Call the OnTextToSpeechOptionsSelectedListener object, if it has been set.
	 */
	public void callOnTextToSpeechOptionsSelectedListener()
	{
		OnTextToSpeechOptionsSelectedListener listener =
			this.getOnTextToSpeechOptionsSelectedListener();

		if (listener != null)
		{
			NumberPicker picker = this.getTtsFrequencyPicker();
			boolean useTts = this.shouldUseTts();
			int freq = picker.getValue();

			listener.onTextToSpeechOptionsSelected(useTts, freq);
		}
	}

	/**
	 * Get the default flag indicating whether text-to-speech should be used or
	 * not.
	 *
	 * @return The default flag indicating whether text-to-speech should be used
	 *     or not.
	 */
	public boolean getDefaultUseTts()
	{
		return this.mDefaultUseTts;
	}

	/**
	 * Get the default text-to-speech frequency.
	 *
	 * @return The default text-to-speech frequency.
	 */
	public int getDefaultTtsFrequency()
	{
		return this.mDefaultTtsFrequency;
	}

	/**
	 * Get the OnTextToSpeechOptionsSelectedListener object.
	 *
	 * @return The OnTextToSpeechOptionsSelectedListener object.
	 */
	public OnTextToSpeechOptionsSelectedListener getOnTextToSpeechOptionsSelectedListener()
	{
		return this.mOnTextToSpeechOptionsSelectedListener;
	}

	/**
	 * Get the checkbox for setting whether text-to-speech should be used or not.
	 *
	 * @return The checkbox for setting whether text-to-speech should be used or not.
	 */
	private MaterialCheckBox getShouldUseTtsCheckBox()
	{
		return this.mShouldUseTtsCheckBox;
	}

	/**
	 * Get the ummary text for whether text-to-speech should be used or not.
	 *
	 * @return The summary text for whether text-to-speech should be used or not.
	 */
	private TextView getShouldUseTtsSummary()
	{
		return this.mShouldUseTtsSummary;
	}

	/**
	 * Get the scrollable picker for the text-to-speech frequency.
	 *
	 * @return The scrollable picker for the text-to-speech frequency.
	 */
	private NumberPicker getTtsFrequencyPicker()
	{
		return this.mTtsFrequencyPicker;
	}

	/**
	 * Called when the check box is clicked.
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();

		if (id == R.id.should_use_tts)
		{
			this.toggleShouldUseTts();
			this.setupShouldUseTtsSummary();
			this.setupTtsFrequencyEnabled();
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
			.setTitle(cons.getTitleTextToSpeech())
			.setPositiveButton(cons.getActionOk(), (dialog, which) ->
				this.callOnTextToSpeechOptionsSelectedListener())
			.setNegativeButton(cons.getActionCancel(), (dialog, which) -> {})
			.setView(R.layout.dlg_alarm_text_to_speech)
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
		RelativeLayout useTtsContainer = dialog.findViewById(R.id.should_use_tts);

		// Set the member variable widgets
		this.mShouldUseTtsCheckBox = dialog.findViewById(R.id.should_use_tts_checkbox);
		this.mShouldUseTtsSummary = dialog.findViewById(R.id.should_use_tts_summary);
		this.mTtsFrequencyPicker = dialog.findViewById(R.id.tts_frequency_picker);

		// Setup the dialog and widgets
		useTtsContainer.setOnClickListener(this);
		this.setupShouldUseTts();
		this.setupTtsFrequencyPicker();
		this.setupTtsFrequencyEnabled();
		this.setupShouldUseTtsColor();
	}

	/**
	 * Set the default flag indicating whether text-to-speech should be used or
	 * not.
	 *
	 * @param  useTts  The default flag indicating whether text-to-speech should
	 *     be used or not
	 */
	public void setDefaultUseTts(boolean useTts)
	{
		this.mDefaultUseTts = useTts;
	}

	/**
	 * Set the default text-to-speech frequency.
	 *
	 * @param  freq  The default text-to-speech frequency.
	 */
	public void setDefaultTtsFrequency(int freq)
	{
		this.mDefaultTtsFrequency = freq;
	}

	/**
	 * Set the OnTextToSpeechOptionsSelectedListener object.
	 *
	 * @param  listener  The OnTextToSpeechOptionsSelectedListener object.
	 */
	public void setOnTextToSpeechOptionsSelectedListener(
		OnTextToSpeechOptionsSelectedListener listener)
	{
		this.mOnTextToSpeechOptionsSelectedListener = listener;
	}

	/**
	 * Setup the summary text for whether text-to-speech should be used or not.
	 */
	private void setupShouldUseTts()
	{
		boolean useTts = this.getDefaultUseTts();

		this.getShouldUseTtsCheckBox().setChecked(useTts);
		this.setupShouldUseTtsSummary();
	}

	/**
	 * Setup the summary text for whether text-to-speech should be used or not.
	 */
	private void setupShouldUseTtsColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		int[] colors = new int[] { shared.getThemeColor(), Color.GRAY };
		int[][] states = new int[][] {
			new int[] {  android.R.attr.state_checked },
			new int[] { -android.R.attr.state_checked } };
		ColorStateList colorStateList = new ColorStateList(states, colors);

		this.getShouldUseTtsCheckBox().setButtonTintList(colorStateList);
	}

	/**
	 * Setup the summary text for whether text-to-speech should be used or not.
	 */
	private void setupShouldUseTtsSummary()
	{
		boolean useTts = this.shouldUseTts();
		int textId = useTts ? R.string.speak_to_me_true : R.string.speak_to_me_false;

		this.getShouldUseTtsSummary().setText(textId);
	}

	/**
	 * Setup whether the text-to-speech frequency container can be used or not.
	 */
	private void setupTtsFrequencyEnabled()
	{
		NumberPicker picker = this.getTtsFrequencyPicker();
		boolean useTts = this.shouldUseTts();

		picker.setAlpha(useTts ? 1.0f : 0.25f);
		picker.setEnabled(useTts);
	}

	/**
	 * Setup scrollable picker for the text-to-speech frequency.
	 */
	private void setupTtsFrequencyPicker()
	{
		NacSharedConstants cons = this.getSharedPreferences().getConstants();
		NumberPicker picker = this.getTtsFrequencyPicker();
		int freq = this.getDefaultTtsFrequency();
		List<String> values = cons.getTextToSpeechFrequency();

		picker.setMinValue(0);
		picker.setMaxValue(values.size()-1);
		picker.setDisplayedValues((String[])values.toArray());
		picker.setValue(freq);
	}

	/**
	 * Get whether text-to-speech is being used or not.
	 *
	 * @return True if text-to-speech is being used, and False otherwise.
	 */
	private boolean shouldUseTts()
	{
		return this.getShouldUseTtsCheckBox().isChecked();
	}

	/**
	 * Toggle whether text-to-speech is being used or not.
	 */
	private void toggleShouldUseTts()
	{
		boolean useTts = this.shouldUseTts();

		this.getShouldUseTtsCheckBox().setChecked(!useTts);
	}

}
