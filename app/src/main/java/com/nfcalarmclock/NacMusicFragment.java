package com.nfcalarmclock;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Display a browser for the user to browse for music files.
 */
public class NacMusicFragment
	extends NacMediaFragment
	implements NacFileBrowser.OnClickListener
{

	/**
	 * Read request callback success result.
	 */
	public static final int READ_REQUEST = 1;

	/**
	 */
	public NacMusicFragment()
	{
		super();
	}

	/**
	 * Create a new instance of this fragment.
	 */
	public static Fragment newInstance(NacAlarm alarm)
	{
		Fragment fragment = new NacMusicFragment();
		Bundle bundle = NacAlarmParcel.toBundle(alarm);

		fragment.setArguments(bundle);

		return fragment;
	}

	/**
	 */
	@Override
	public void onClick(NacFileBrowser browser, File file, String path,
		String name)
	{
		if (file.isDirectory())
		{
			browser.show(path);
		}
		else if (file.isFile())
		{
			NacMediaPlayer player = this.getMediaPlayer();

			this.setMedia(path);
			player.reset();
			player.play(path);
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
		String path = this.getSoundPath();
		String parentPath = "";
		File parentFile = new File(path).getParentFile();

		if ((parentFile != null) && NacSound.isFile(path))
		{

			try
			{
				parentPath = parentFile.getCanonicalPath();
			}
			catch (IOException e)
			{
			}
		}

		browser.setOnClickListener(this);
		browser.show(parentPath);
	}

}
