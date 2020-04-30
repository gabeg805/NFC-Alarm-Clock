package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

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
	 * Text view showing the current directory.
	 */
	private TextView mDirectoryTextView;

	/**
	 * Read request callback success result.
	 */
	public static final int READ_REQUEST_CODE = 1;

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
	 * @return The view displaying the current directory path.
	 */
	public TextView getDirectoryTextView()
	{
		return this.mDirectoryTextView;
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
	public static Fragment newInstance(String media)
	{
		Fragment fragment = new NacMusicFragment();
		Bundle bundle = NacBundle.toBundle(media);

		fragment.setArguments(bundle);
		return fragment;
	}

	/**
	 */
	@Override
	public void onBuildDialog(NacDialog dialog, AlertDialog.Builder builder)
	{
		Context context = dialog.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);

		builder.setTitle(cons.getFolderSelected());
		dialog.setPositiveButton(cons.getOk());
		dialog.setNegativeButton(cons.getCancel());
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
			String path = getMediaPath();

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
			String textPath = path.isEmpty() ? "" : String.format("%s/", path);

			this.setMedia(path);
			this.getDirectoryTextView().setText(textPath);
			browser.show(path);
		}
		else if (metadata.isFile())
		{
			Context context = getContext();
			Uri uri = metadata.toExternalUri();

			if (browser.isSelected())
			{
				if (this.safePlay(uri, true) < 0)
				{
					NacSharedConstants cons = new NacSharedConstants(context);
					NacUtility.printf("Unable to play music : %d | %s",
						metadata.getId(), metadata.getPath());
					NacUtility.toast(context, cons.getPlayAudioError());
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
		Context context = getContext();

		if (!NacPermissions.hasRead(context))
		{
			NacPermissions.requestRead(context, READ_REQUEST_CODE);
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
		Context context = getContext();
		NacFileBrowser browser = new NacFileBrowser(root, R.id.container);
		TextView textview = (TextView) root.findViewById(R.id.path);
		String contentPath = getMediaPath();
		String dirPath = contentPath;
		String filePath = "";
		this.mDirectoryTextView = textview;
		this.mFileBrowser = browser;

		if (NacMedia.isFile(context, contentPath))
		{
			String name = NacMedia.getName(context, contentPath);
			dirPath = NacMedia.getRelativePath(context, contentPath);
			filePath = String.format("%s%s", dirPath, name);
		}
		else if (NacMedia.isDirectory(contentPath))
		{
			dirPath = contentPath;
		}
		else
		{
			dirPath = "";
		}

		this.getDirectoryTextView().setText(dirPath);
		browser.setOnClickListener(this);
		browser.show(dirPath, filePath);
	}

}
