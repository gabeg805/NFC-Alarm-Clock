package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import java.util.ArrayList;
import java.util.List;

public class NacSoundRingtoneDialog
	extends NacSoundDialog
{

	/**
	 */
	public NacSoundRingtoneDialog()
	{
		super();
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		String title = context.getString(R.string.dlg_ringtone_title);

		builder.setTitle(title);
		super.onBuildDialog(context, builder);
	}

	/**
	 * @return The list of sounds for the ringtone manager.
	 */
	@Override
	protected List<NacSound> getSoundList(Context context)
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

			list.add(new NacSound(id, dir, name));
		}

		return list;
	}

}
