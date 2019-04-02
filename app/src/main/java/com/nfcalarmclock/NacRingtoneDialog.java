package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Display a dialog that shows a list of alarm ringtones.
 */
public class NacRingtoneDialog
	extends NacMediaDialog
	implements CompoundButton.OnCheckedChangeListener,
		NacDialog.OnNeutralActionListener,
		NacDialog.OnShowListener
{

	/**
	 */
	public NacRingtoneDialog()
	{
		super();

		this.addOnShowListener(this);
		this.addOnNeutralActionListener(this);
	}

	/**
	 * Build using a layout for displaying music.
	 */
	@Override
	public AlertDialog.Builder build(Context context)
	{
		return this.build(context, R.layout.dlg_sound_ringtone);
	}

	/**
	 * @return The RadioButton group.
	 */
	private RadioGroup getRadioGroup(NacDialog dialog)
	{
		return (RadioGroup) dialog.getRoot().findViewById(R.id.radio_group);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		super.onBuildDialog(context, builder);

		String title = context.getString(R.string.dlg_ringtone_title);

		builder.setTitle(title);
	}

	/**
	 * Handle selection of radio button.
	 */
	@Override
	public void onCheckedChanged(CompoundButton b, boolean state)
	{
		if (!state)
		{
			return;
		}

		String path = (String) b.getTag();
		String name = b.getText().toString();

		this.play(path, name);
	}

	/**
	 * Clear the selected item.
	 */
	@Override
	public boolean onNeutralActionDialog(NacDialog dialog)
	{
		super.onNeutralActionDialog(dialog);

		RadioGroup group = this.getRadioGroup(dialog);

		group.clearCheck();

		return true;
	}

	/**
	 * Setup views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		Context context = root.getContext();
		List<NacMedia.Pair> ringtones = NacMedia.getRingtones(context);
		RadioGroup group = this.getRadioGroup(dialog);
		NacAlarm alarm = (NacAlarm) this.getData();
		String path = alarm.getSound();

		for(int i=0; i < ringtones.size(); i++)
		{
			RadioButton button = new RadioButton(context);
			NacMedia.Pair pair = ringtones.get(i);

			group.addView(button);
			button.setText(pair.getName());
			button.setTag(pair.getPath());

			if (path.equals(pair.getPath()))
			{
				button.setChecked(true);
			}

			button.setOnCheckedChangeListener(this);
		}
	}

	/**
	 * Scale the dialog.
	 */
	@Override
	public void scale()
	{
		this.scale(0.7, 0.8, false, true);
	}

}
