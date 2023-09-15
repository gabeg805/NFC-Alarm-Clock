package com.nfcalarmclock.mediapicker.music;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.nfcalarmclock.media.NacMedia;
import com.nfcalarmclock.permission.readmediaaudio.NacReadMediaAudioPermission;
import com.nfcalarmclock.util.dialog.NacDialog;
import com.nfcalarmclock.file.NacFile;
import com.nfcalarmclock.file.browser.NacFileBrowser;
import com.nfcalarmclock.R;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.mediapicker.NacMediaFragment;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.system.NacBundle;
import java.util.Locale;

/**
 * Display a browser for the user to browse for music files.
 */
public class NacMusicFragment
	extends NacMediaFragment
	implements NacFileBrowser.OnBrowserClickedListener,
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

		builder.setTitle(cons.getTitleFolderSelected());
		dialog.setPositiveButton(cons.getActionOk());
		dialog.setNegativeButton(cons.getActionCancel());
	}

	/**
	 */
	@Override
	public void onBrowserClicked(NacFileBrowser browser,
		NacFile.Metadata metadata, String path, String name)
	{
		// Directory was clicked
		if (metadata.isDirectory())
		{
			Locale locale = Locale.getDefault();
			String textPath = path.isEmpty() ? ""
				: String.format(locale, "%1$s/", path);

			// Set the alarm media path to the directory
			this.setMedia(path);
			this.getDirectoryTextView().setText(textPath);

			// Show the contents of the directory
			browser.show(path);
		}
		// File was clicked
		else if (metadata.isFile())
		{
			Uri uri = metadata.toExternalUri();

			// Play the media file
			if (browser.isSelected())
			{
				if (this.safePlay(uri) < 0)
				{
					// There was an error playing the media
					this.showErrorPlayingAudio();
				}
			}
			// Stop playing the media file
			else
			{
				this.safeReset();
			}
		}
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();

		// Clear button was clicked
		if (id == R.id.clear)
		{
			NacFileBrowser browser = this.getFileBrowser();

			// De-select whatever is selected
			if (browser != null)
			{
				browser.deselect();
			}
		}
		// OK button was clicked
		else if (id == R.id.ok)
		{
			String path = getMediaPath();

			// Check if the a directory was selected. If so, show a warning.
			// The path has already been set in onBrowserClicked() so nothing
			// further needs to be done
			if (NacMedia.isDirectory(path))
			{
				this.showWarningDirectorySelected(view);
				return;
			}
		}

		// Default onClick() method
		super.onClick(view);
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
		//Context context = getContext();

		//if (!NacPermissions.hasRead(context))
		//{
		//	NacPermissions.requestRead(context, READ_REQUEST_CODE);
		//}
	}

	/**
	 */
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
	{
		setupActionButtons(view);

		if (!NacReadMediaAudioPermission.hasPermission(getContext()))
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
		NacFileBrowser browser = new NacFileBrowser(this, root, R.id.container);
		TextView textview = root.findViewById(R.id.path);
		String path = getMediaPath();
		String dir = "";
		String name = "";
		this.mDirectoryTextView = textview;
		this.mFileBrowser = browser;

		if (NacMedia.isFile(context, path))
		{
			Uri uri = Uri.parse(path);
			dir = NacMedia.getRelativePath(context, uri);
			name = NacMedia.getName(context, uri);
		}
		else if (NacMedia.isDirectory(path))
		{
			dir = path;
		}

		this.getDirectoryTextView().setText(dir);
		browser.setOnBrowserClickedListener(this);
		browser.show(dir);
		browser.select(name);
	}

	/**
	 * Show a warning indicating that a music directory was selected.
	 */
	public void showWarningDirectorySelected(View view)
	{
		Context context = getContext();
		NacDialog dialog = new NacDialog();

		dialog.saveData(view);
		dialog.setOnBuildListener(this);
		dialog.addOnDismissListener(this);
		dialog.build(context, R.layout.dlg_media_playlist);
		dialog.show();
		dialog.scale(0.85, 1.0, false, true);
	}

}
