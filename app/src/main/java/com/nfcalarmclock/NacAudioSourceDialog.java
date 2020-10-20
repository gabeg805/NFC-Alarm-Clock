package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.List;

/**
 * The dialog class to handle choosing a media source for the alarm sound.
 */
public class NacAudioSourceDialog
	extends NacDialog
	implements NacDialog.OnDismissListener,
		NacDialog.OnShowListener
{

	/**
	 * Radio button group for each alarm source.
	 */
	private RadioGroup mRadioGroup;

	/**
	 * Constants.
	 */
	private NacSharedConstants mConstants;

	/**
	 */
	public NacAudioSourceDialog()
	{
		super(R.layout.dlg_alarm_audio_source);

		this.mRadioGroup = null;
		this.mConstants = null;

		addOnDismissListener(this);
		addOnShowListener(this);
	}

	/**
	 */
	@Override
	public AlertDialog.Builder build(Context context)
	{
		this.mConstants = new NacSharedConstants(context);
		return super.build(context);
	}

	/**
	 * @return The list of audio sources.
	 */
	private List<String> getAudioSources()
	{
		return this.getConstants().getAudioSources();
	}

	/**
	 * @return The radio button at the given index.
	 */
	public RadioButton getRadioButton(int index)
	{
		RadioGroup group = this.getRadioGroup();
		return (RadioButton) group.getChildAt(index);
	}

	/**
	 * @return The audio sources radio group.
	 */
	public RadioGroup getRadioGroup()
	{
		return this.mRadioGroup;
	}

	/**
	 * @return The active radio button.
	 */
	public RadioButton getCheckedButton()
	{
		RadioGroup group = this.getRadioGroup();
		int checkedId = group.getCheckedRadioButtonId();
		return this.getRoot().findViewById(checkedId);
	}

	/**
	 * @return The constants.
	 */
	private NacSharedConstants getConstants()
	{
		return this.mConstants;
	}

	/**
	 * Inflate audio source radio buttons.
	 */
	private void inflateRadioButtons()
	{
		Context context = this.getContext();
		RadioGroup group = this.getRadioGroup();
		List<String> sources = this.getAudioSources();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);

		for (String s : sources)
		{
			View view = inflater.inflate(R.layout.radio_button, group, true);
			RadioButton button = view.findViewById(R.id.radio_button);

			button.setId(button.generateViewId());
			button.setText(s);
		}
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		View root = this.getRoot();
		NacSharedConstants cons = this.getConstants();
		this.mRadioGroup = root.findViewById(R.id.audio_sources);

		builder.setTitle(cons.getTitleAudioSource());
		setPositiveButton(cons.getActionOk());
		setNegativeButton(cons.getActionCancel());
		this.inflateRadioButtons();
	}

	/**
	 * Save the audio source index as the dialog data.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		RadioButton button = this.getCheckedButton();
		String source = button.getText().toString();
		String data = (source == null) || source.isEmpty()
			? this.getAudioSources().get(1) : source;

		dialog.saveData(data);
		return true;
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		this.setCheckedRadioButton();
		scale(0.7, 0.7, false, true);
	}

	/**
	 * Set the checked radio button.
	 */
	protected void setCheckedRadioButton()
	{
		Context context = this.getContext();
		String data = this.getDataString();
		List<String> sources = this.getAudioSources();

		this.getRadioButton(1).setChecked(true);

		for (int i=0; i < sources.size(); i++)
		{
			if (data.equals(sources.get(i)))
			{
				this.getRadioButton(i).setChecked(true);
				break;
			}
		}
	}

}
