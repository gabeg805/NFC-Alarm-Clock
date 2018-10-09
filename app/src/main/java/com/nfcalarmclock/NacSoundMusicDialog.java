package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Music dialog when selecting an alarm sound.
 */
public class NacSoundMusicDialog
	extends NacSoundDialog
	implements View.OnLongClickListener,NacPermissions.OnResultListener
{

	/**
	 * Request value for Read permissions.
	 */
	private static final int NAC_SOUND_READ_REQUEST = 1;

	/**
	 */
	public NacSoundMusicDialog()
	{
		super();
		super.setOnLongClickListener(this);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		String title = context.getString(R.string.dlg_music_title);

		builder.setTitle(title);
		super.onBuildDialog(context, builder);
	}

	/**
	 * Show path of sound when long clicked.
	 */
	@Override
	public boolean onLongClick(View view)
	{
		Context context = view.getContext();
		int index = (int) view.getTag();
		String path = this.mSounds.get(index).path;

		Toast.makeText(context, path, Toast.LENGTH_LONG).show();

		return true;
	}

	/**
	 */
	@Override
	public void onResult(int request, String[] permissions, int[] grant)
	{
		NacUtility.printf("OnResult listener worked!");

		if (request == NAC_SOUND_READ_REQUEST)
		{
			if ((grant.length > 0)
				&& (grant[0] == PackageManager.PERMISSION_GRANTED))
			{
				this.show();
			}
			else
			{
				this.dismiss();
			}
		}
	}
	/**
	 * Setup views for when the dialog is shown.
	 */
	@Override
	public void onShowDialog(Context context, View root)
	{
		if (!NacPermissions.hasRead(context))
		{
			NacPermissions.request(context,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				NAC_SOUND_READ_REQUEST);
			NacPermissions.setResultListener(context, this);
			this.hide();
			return;
		}

		super.onShowDialog(context, root);
	}

	/**
	 * Setup the dialog's list of music.
	 */
	@Override
	protected List<NacSound> getSoundList(Context context)
	{
		List<NacSound> list = new ArrayList<>();
		String root = Environment.getExternalStorageDirectory().toString();
		String[] search = {"Download", "Music", "Playlists"};
		FilenameFilter filter = this.getFilter();

		for (String d : search)
		{
			String dir = root+"/"+d;
			File obj = new File(dir);
			File[] files = obj.listFiles(filter);

			for (int i=0; (files != null) && (i < files.length); i++)
			{
				String name = files[i].getName();

				list.add(new NacSound(name, dir));
			}
		}

		return list;
	}

}
