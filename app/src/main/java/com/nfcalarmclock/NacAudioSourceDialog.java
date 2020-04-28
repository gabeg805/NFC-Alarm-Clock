package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
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
	 * Defaults.
	 */
	private NacSharedDefaults mDefaults;

	/**
	 */
	public NacAudioSourceDialog()
	{
		super();

		this.mRadioGroup = null;
		this.mDefaults = null;

		addOnDismissListener(this);
		addOnShowListener(this);
	}

	/**
	 * Build the dialog.
	 */
	public void build(Context context)
	{
		this.mDefaults = new NacSharedDefaults(context);
		this.build(context, R.layout.dlg_alarm_audio_source);
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
	 * @return The defaults.
	 */
	private NacSharedDefaults getDefaults()
	{
		return this.mDefaults;
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
	 * Save the audio source index as the dialog data.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacSharedDefaults defaults = this.getDefaults();
		RadioButton button = this.getCheckedButton();
		String source = button.getText().toString();
		String data = (source == null) || source.isEmpty()
			? defaults.getAudioSources().get(1) : source;

		dialog.saveData(data);
		return true;
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		Context context = root.getContext();
		RadioGroup group = (RadioGroup) root.findViewById(R.id.audio_sources);
		String data = this.getDataString();
		NacSharedDefaults defaults = this.getDefaults();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		List<String> audioSources = defaults.getAudioSources();

		if (data.equals(audioSources.get(0)))
		{
			group.check(R.id.alarm);
		}
		else if (data.equals(audioSources.get(1)))
		{
			group.check(R.id.media);
		}
		else if (data.equals(audioSources.get(2)))
		{
			group.check(R.id.notification);
		}
		else if (data.equals(audioSources.get(3)))
		{
			group.check(R.id.ringtone);
		}
		else if (data.equals(audioSources.get(4)))
		{
			group.check(R.id.system);
		}
		else
		{
			group.check(R.id.media);
		}

		this.mRadioGroup = group;
		scale(0.7, 0.7, false, true);
	}

}
