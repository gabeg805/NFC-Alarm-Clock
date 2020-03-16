package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.Manifest;
import android.net.Uri;
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
		NacFileBrowser browser = this.getFileBrowser();

		return (browser != null) ? browser.previousDirectory() : false;
	}

	/**
	 * @return The file browser.
	 */
	public NacFileBrowser getFileBrowser()
	{
		return this.mFileBrowser;
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
			NacFileBrowser browser = this.getFileBrowser();

			if (browser != null)
			{
				browser.deselect();
			}
		}
		else if (id == R.id.ok)
		{
			String path = getSoundPath();

			if (NacMedia.isDirectory(path))
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
	public void onClick(NacFileBrowser browser, NacFile.Metadata metadata,
		String path, String name)
	{
		if (metadata.isDirectory())
		{
			this.setMedia(path);
			browser.show(path);
		}
		else if (metadata.isFile())
		{
			Context context = getContext();
			Uri playUri = NacMedia.toUri(metadata);

			if (browser.isSelected())
			{
				if (this.safePlay(playUri, true) < 0)
				{
					NacUtility.toast(context, "Unable to play music");
				}
			}
			else
			{
				this.safeReset();
			}
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
			NacUtility.toast(getContext(), "You don't have read permissions!");
			return;
		}

		setupFileBrowser(view);
	}

	/**
	 * Setup the file browser.
	 */
	private void setupFileBrowser(View root)
	{
		Context context = getContext();
		NacFileBrowser browser = new NacFileBrowser(root, R.id.path,
			R.id.container);
		String home = NacFileBrowser.getHome();
		String path = getSoundPath();
		this.mFileBrowser = browser;
		String absolutePath = home;
		String filePath = "";

		if (NacMedia.isDirectory(path))
		{
			absolutePath = path;
		}
		else if (NacMedia.isFile(context, path))
		{
			String relativePath = NacMedia.getRelativePath(context, path);
			String name = NacMedia.getName(context, path);

			absolutePath = String.format("%s/%s", home, relativePath);
			filePath = String.format("%s%s", absolutePath, name);
		}

		browser.setOnClickListener(this);
		browser.show(absolutePath, filePath);
	}

}
