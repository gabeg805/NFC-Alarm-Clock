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
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		NacSharedConstants cons = new NacSharedConstants(context);

		builder.setTitle(cons.getTitleAudioSource());
		setPositiveButton(cons.getActionOk());
		setNegativeButton(cons.getActionCancel());
	}

	/**
	 * Save the audio source index as the dialog data.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacSharedConstants cons = this.getConstants();
		RadioButton button = this.getCheckedButton();
		String source = button.getText().toString();
		String data = (source == null) || source.isEmpty()
			? cons.getAudioSources().get(1) : source;

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
		NacSharedConstants cons = this.getConstants();
		List<String> audioSources = cons.getAudioSources();

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
