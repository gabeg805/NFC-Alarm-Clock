package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Display a browser for the user to browse for music files.
 */
public class NacMusicFragment
	extends NacMediaFragment
	implements NacFileBrowser.OnClickListener,
		NacDialog.OnBuildListener,
		NacDialog.OnDismissListener
{

	/**
	 * File browser.
	 */
	private NacFileBrowser mFileBrowser;

	/**
	 * Read request callback success result.
	 */
	public static final int READ_REQUEST = 1;

	/**
	 */
	public NacMusicFragment()
	{
		super();

		this.mFileBrowser = null;
	}

	/**
	 * Back button was been pressed.
	 */
	public boolean backPressed()
	{
		return this.mFileBrowser.previousDirectory();
	}

	/**
	 * Create a new instance of this fragment.
	 */
	public static Fragment newInstance(NacAlarm alarm)
	{
		Fragment fragment = new NacMusicFragment();
		Bundle bundle = NacBundle.toBundle(alarm);

		fragment.setArguments(bundle);

		return fragment;
	}

	/**
	 * Create a new instance of this fragment.
	 */
	public static Fragment newInstance(NacSound sound)
	{
		Fragment fragment = new NacMusicFragment();
		Bundle bundle = NacBundle.toBundle(sound);

		fragment.setArguments(bundle);

		return fragment;
	}

	/**
	 */
	@Override
	public void onBuildDialog(NacDialog dialog, AlertDialog.Builder builder)
	{
		builder.setTitle("You Selected a Folder");

		dialog.setPositiveButton("YES");
		dialog.setNegativeButton("NO");
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();

		if (id == R.id.clear)
		{
			this.mFileBrowser.deselect();
		}
		else if (id == R.id.ok)
		{
			String path = getSoundPath();
			File file = new File(path);

			if (file.isDirectory())
			{
				Context context = getContext();
				NacDialog dialog = new NacDialog();

				dialog.saveData(view);
				dialog.setOnBuildListener(this);
				dialog.addOnDismissListener(this);
				dialog.build(context, R.layout.dlg_media_playlist);
				dialog.show();
				dialog.scale(0.85, 1.0, false, true);

				return;
			}
		}

		super.onClick(view);
	}

	/**
	 */
	@Override
	public void onClick(NacFileBrowser browser, File file, String path,
		String name)
	{
		if (file.isDirectory())
		{
			this.setMedia(path);
			browser.show(path);
		}
		else if (file.isFile())
		{
			path = (browser.isSelected()) ? path : "";

			if (this.safePlay(path, true) < 0)
			{
				NacUtility.toast(getContext(), "Unable to play music");
			}

			//NacMediaPlayer player = this.getMediaPlayer();

			//player.reset();

			//if (browser.isSelected())
			//{
			//	this.setMedia(path);
			//	player.play(path, true);
			//}
			//else
			//{
			//	this.setMedia("");
			//}
		}
	}

	/**
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.frg_music, container, false);
	}

	/**
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		View view = (View) dialog.getData();

		super.onClick(view);

		return true;
	}

	/**
	 * Prompt the user to enable read permissions.
	 */
	@Override
	protected void onSelected()
	{
		if (!NacPermissions.hasRead(getContext()))
		{
			NacPermissions.request(getActivity(),
				Manifest.permission.READ_EXTERNAL_STORAGE, READ_REQUEST);
		}
	}

	/**
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		setupActionButtons(view);

		if (!NacPermissions.hasRead(getContext()))
		{
			return;
		}

		setupFileBrowser(view);
	}

	/**
	 * Setup the file browser.
	 */
	private void setupFileBrowser(View root)
	{
		NacFileBrowser browser = new NacFileBrowser(root, R.id.path,
			R.id.group);
		String sound = getSoundPath();
		String path = sound;
		File pathFile = new File(path);
		this.mFileBrowser = browser;

		if (NacSound.isFilePlaylist(path))
		{
			;
		}
		else if (NacSound.isFile(path) && pathFile.isFile())
		{
			File parentFile = pathFile.getParentFile();

			if (parentFile != null)
			{

				try
				{
					path = parentFile.getCanonicalPath();
				}
				catch (IOException e)
				{
				}
			}
		}
		else
		{
			path = NacFileBrowser.getHome();
		}

		browser.setOnClickListener(this);
		browser.show(path);
		browser.select(sound);
	}

}
