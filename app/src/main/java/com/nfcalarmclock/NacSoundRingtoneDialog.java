package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.ArrayList;
import java.util.List;

public class NacSoundRingtoneDialog
	extends NacSoundDialog
	implements CompoundButton.OnCheckedChangeListener
{

	/**
	 */
	public NacSoundRingtoneDialog()
	{
		super();
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
	 * @return The list of ringtones for the ringtone manager.
	 */
	public List<NacSound> getRingtones(Context context)
	{
		RingtoneManager manager = new RingtoneManager(context);
		List<NacSound> list = new ArrayList<>();

		manager.setType(RingtoneManager.TYPE_ALARM);

		Cursor cursor = manager.getCursor();

		while (cursor.moveToNext())
		{
			String name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
			String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
			String dir = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);

			if (this.containsName(list, name))
			{
				continue;
			}

			list.add(new NacSound(dir, id, name));
		}

		return list;
	}

	/**
	 * Check if the sound list contains the name of the given sound.
	 */
	protected boolean containsName(List<NacSound> sounds, String name)
	{
		for (NacSound s : sounds)
		{
			if (s.containsName(name))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		NacUtility.printf("onBuildDialog in NacSoundRingtoneDialog");
		String title = context.getString(R.string.dlg_ringtone_title);

		builder.setTitle(title);
		super.onBuildDialog(context, builder);
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

		this.play((NacSound) b.getTag());
	}

	/**
	 * Setup views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(Context context, View root)
	{
		NacUtility.printf("onShowDialog in NacSoundRingtoneDialog");
		RadioGroup group = (RadioGroup) root.findViewById(R.id.radio_group);
		List<NacSound> ringtones = this.getRingtones(context);
		RadioButton button;
		NacSound sound;

		for(int i=0; i < ringtones.size(); i++)
		{
			button = new RadioButton(context);
			sound = ringtones.get(i);

			button.setText(sound.name);
			button.setTag(sound);
			button.setOnCheckedChangeListener(this);

			group.addView(button);
		}
	}

	/**
	 * Scale the dialog.
	 */
	@Override
	public void scale()
	{
		NacUtility.printf("Scaling NacSoundRingtoneDialog");
		this.scale(0.7, 0.8, false, true);
	}

}
