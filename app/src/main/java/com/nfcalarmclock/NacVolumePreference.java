package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

/**
 * Preference to choose the default volume and audio source.
 */
public class NacVolumePreference
	extends Preference
	implements View.OnClickListener,
		NacDialog.OnDismissListener,
		SeekBar.OnSeekBarChangeListener
{

	/**
	 * Volume level.
	 */
	protected int mValue;

	/**
	 * Audio source.
	 */
	protected String mSource;

	/**
	 * Seekbar.
	 */
	protected SeekBar mSeek;

	/**
	 * Shared preferences.
	 */
	protected NacSharedPreferences mShared;

	/**
	 */
	public NacVolumePreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacVolumePreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacVolumePreference(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference_volume);
	}

	/**
	 */
	@Override
	public void onAttached()
	{
		super.onAttached();

		Context context = getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		this.mSource = shared.getAudioSource();
		this.mShared = shared;
	}

	/**
	 */
	@Override
	public void onBindViewHolder(PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);

		this.mSeek = (SeekBar) holder.findViewById(R.id.volume_slider);
		RelativeLayout image = (RelativeLayout) holder.findViewById(R.id.widget);

		this.mSeek.setProgress(this.mValue);
		this.mSeek.setOnSeekBarChangeListener(this);
		image.setOnClickListener(this);
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		NacAudioSourceDialog dialog = new NacAudioSourceDialog();
		Context context = getContext();

		dialog.build(context);
		dialog.saveData(this.mSource);
		dialog.addOnDismissListener(this);
		dialog.show();
	}

	/**
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		this.mSource = dialog.getDataString();
		this.mShared.editAudioSource(this.mSource);
		return true;
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		Context context = getContext();
		NacSharedDefaults defaults = new NacSharedDefaults(context);
		return (Integer) a.getInteger(index, defaults.getVolume());
	}

	/**
	 * Set the initial preference value.
	 */
	@Override
	protected void onSetInitialValue(Object defaultValue)
	{
		if (defaultValue == null)
		{
			this.mValue = getPersistedInt(this.mValue);
		}
		else
		{
			this.mValue = (Integer) defaultValue;
			persistInt(this.mValue);
		}
	}

	/**
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
		boolean fromUser)
	{
		this.mValue = progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		persistInt(this.mValue);
	}

}
