package com.nfcalarmclock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import androidx.fragment.app.DialogFragment;

import android.content.res.ColorStateList;
import android.graphics.Color;

/**
 */
public class NacAlarmAudioSourceDialog
	extends DialogFragment
{

	/**
	 * Listener for when an audio source is selected.
	 */
	public interface OnAudioSourceSelectedListener
	{
		public void onAudioSourceSelected(String audioSource);
	}

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacAlarmAudioSourceDialog";

	/**
	 * Default audio source.
	 */
	private String mDefaultAudioSource;

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Listener for when an audio source is selected.
	 */
	private OnAudioSourceSelectedListener mOnAudioSourceSelectedListener;

	/**
	 * Call the OnAudioSourceSelectedListener object, if it has been set.
	 */
	public void callOnAudioSourceSelectedListener()
	{
		OnAudioSourceSelectedListener listener =
			this.getOnAudioSourceSelectedListener();

		if (listener != null)
		{
			String audioSource = this.getAudioSource();

			listener.onAudioSourceSelected(audioSource);
		}
	}

	/**
	 * Get the selected audio source.
	 *
	 * @return The selected audio source.
	 */
	public String getAudioSource()
	{
		ListView listview = ((AlertDialog) getDialog()).getListView();
		ListAdapter adapter = listview.getAdapter();
		int position = listview.getCheckedItemPosition();

		return (String) adapter.getItem(position);
	}

	/**
	 * Get the default audio source.
	 *
	 * @return The default audio source.
	 */
	public String getDefaultAudioSource()
	{
		return this.mDefaultAudioSource;
	}

	/**
	 * Get the shared preferences.
	 *
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 * Get the OnAudioSourceSelectedListener object.
	 *
	 * @return The OnAudioSourceSelectedListener object.
	 */
	public OnAudioSourceSelectedListener getOnAudioSourceSelectedListener()
	{
		return this.mOnAudioSourceSelectedListener;
	}

	/**
	 */
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
	{
		return new AlertDialog.Builder(requireContext())
			.setTitle(getString(R.string.title_audio_source))
			.setSingleChoiceItems(R.array.audio_sources, -1, (dialog, which) -> {})
			.setPositiveButton(getString(R.string.action_ok), (dialog, which) ->
				this.callOnAudioSourceSelectedListener())
			.setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> {})
			.create();
	}

	/**
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		Context context = getContext();
		this.mSharedPreferences = new NacSharedPreferences(context);

		this.setupAudioSource();
		this.setupAudioSourceColor();
		this.setupDialogColor();
	}

	/**
	 * Set the default audio source.
	 *
	 * @param  audioSource  The default audio source.
	 */
	public void setDefaultAudioSource(String audioSource)
	{
		this.mDefaultAudioSource = audioSource;
	}

	/**
	 * Set the OnAudioSourceSelectedListener object.
	 *
	 * @param  listener  The OnAudioSourceSelectedListener object.
	 *
	 * @return This class.
	 */
	public DialogFragment setOnAudioSourceSelectedListener(
		OnAudioSourceSelectedListener listener)
	{
		this.mOnAudioSourceSelectedListener = listener;
		return this;
	}

	/**
	 * Setup the audio source.
	 */
	private void setupAudioSource()
	{
		AlertDialog dialog = (AlertDialog) getDialog();
		ListView listview = dialog.getListView();
		ListAdapter adapter = listview.getAdapter();
		String audioSource = this.getDefaultAudioSource();

		for (int i=0; i < adapter.getCount(); i++)
		{
			String item = (String) adapter.getItem(i);

			if (item.equals(audioSource))
			{
				listview.setItemChecked(i, true);
				return;
			}
		}
	}

	/**
	 * Setup the color of the audio source items.
	 */
	private void setupAudioSourceColor()
	{
		//ListView listview = ((AlertDialog) getDialog()).getListView();
		//ListAdapter adapter = listview.getAdapter();

		//NacUtility.printf("List view count : %d | %d", listview.getChildCount(), adapter.getCount());

		//for (int i=0; i < adapter.getCount(); i++)
		//{
		//	//RadioButton item = (RadioButton) adapter.getItem(i);
		//}

		//int[] colors = new int[] { themeColor, Color.GRAY };
		//int[][] states = new int[][] {
		//	new int[] {  android.R.attr.state_checked },
		//	new int[] { -android.R.attr.state_checked } };
		//ColorStateList stateList = new ColorStateList(states, colors);

		////radioButton.setButtonTintList(stateList);
	}

	/**
	 * Setup the dialog color.
	 */
	private void setupDialogColor()
	{
		AlertDialog dialog = (AlertDialog) getDialog();
		Button okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		Button cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		int themeColor = this.getSharedPreferences().getThemeColor();

		okButton.setTextColor(themeColor);
		cancelButton.setTextColor(themeColor);
	}

}

