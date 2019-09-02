package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * The dialog class to handle choosing a media source for the alarm sound.
 */
public class NacAudioSourceDialog
	extends NacDialog
	implements RadioGroup.OnCheckedChangeListener,
		NacDialog.OnDismissListener,
		NacDialog.OnShowListener
{

	/**
	 * Radio button group for each alarm source.
	 */
	private RadioGroup mAudioSources;

	/**
	 */
	public NacAudioSourceDialog()
	{
		super();

		this.mAudioSources = null;

		addOnDismissListener(this);
		addOnShowListener(this);
	}

	/**
	 * Build the dialog.
	 */
	public void build(Context context)
	{
		this.build(context, R.layout.dlg_alarm_audio_source);
	}

	/**
	 * @return The active radio button.
	 */
	public RadioButton getCheckedButton()
	{
		int checkedId = this.mAudioSources.getCheckedRadioButtonId();

		return this.getRoot().findViewById(checkedId);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		String title = "Choose an audio source";

		builder.setTitle(title);
		setPositiveButton("OK");
		setNegativeButton("Cancel");
	}

	/**
	 */
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		RadioButton button = this.getCheckedButton();
		String source = button.getText().toString();
	}

	/**
	 * Save the audio source index as the dialog data.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		RadioButton button = this.getCheckedButton();
		String source = button.getText().toString();

		dialog.saveData(((source == null) || source.isEmpty())
			? NacSharedPreferences.DEFAULT_AUDIO_SOURCE : source);

		return true;
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		Context context = root.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		String source = this.getDataString();
		this.mAudioSources = (RadioGroup) root.findViewById(R.id.audio_sources);

		this.mAudioSources.setOnCheckedChangeListener(this);

		if (source.equals("Alarm"))
		{
			this.mAudioSources.check(R.id.alarm);
		}
		else if (source.equals("Music"))
		{
			this.mAudioSources.check(R.id.music);
		}
		else if (source.equals("Notification"))
		{
			this.mAudioSources.check(R.id.notification);
		}
		else if (source.equals("Ringer"))
		{
			this.mAudioSources.check(R.id.ringer);
		}
		else if (source.equals("System"))
		{
			this.mAudioSources.check(R.id.system);
		}
		else
		{
			this.mAudioSources.check(R.id.music);
		}

		scale(0.7, 0.7, false, true);
	}

}
