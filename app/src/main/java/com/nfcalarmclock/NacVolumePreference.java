package com.nfcalarmclock;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.os.Build;
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
	protected String mAudioSource;

	/**
	 * Seekbar.
	 */
	protected SeekBar mVolumeSeekBar;

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
	 * @return Audio source.
	 */
	private String getAudioSource()
	{
		return this.mAudioSource;
	}

	/**
	 * @return Volume seekbar.
	 */
	private SeekBar getVolumeSeekBar()
	{
		return this.mVolumeSeekBar;
	}

	/**
	 * @return Shared preferences.
	 */
	private NacSharedPreferences getShared()
	{
		return this.mShared;
	}

	/**
	 */
	@Override
	public void onAttached()
	{
		super.onAttached();

		Context context = getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		this.mAudioSource = shared.getAudioSource();
		this.mShared = shared;
	}

	/**
	 */
	@Override
	public void onBindViewHolder(PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);

		SeekBar seekbar = (SeekBar) holder.findViewById(R.id.volume_slider);
		RelativeLayout image = (RelativeLayout) holder.findViewById(R.id.widget);
		this.mVolumeSeekBar = seekbar;

		seekbar.setProgress(this.mValue);
		seekbar.setOnSeekBarChangeListener(this);
		image.setOnClickListener(this);
		this.setSeekBarColor();
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		Context context = getContext();
		String audioSource = this.getAudioSource();
		NacAudioSourceDialog dialog = new NacAudioSourceDialog();

		dialog.build(context);
		dialog.saveData(audioSource);
		dialog.addOnDismissListener(this);
		dialog.show();
	}

	/**
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacSharedPreferences shared = this.getShared();
		String audioSource = dialog.getDataString();
		this.mAudioSource = audioSource;

		shared.editAudioSource(audioSource);
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

	/**
	 * Set the volume seekbar color.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.Q)
	@SuppressLint("NewApi")
	public void setSeekBarColor()
	{
		NacSharedPreferences shared = this.getShared();
		SeekBar seekbar = this.getVolumeSeekBar();
		int themeColor = shared.getThemeColor();
		Drawable progressDrawable = seekbar.getProgressDrawable();
		Drawable thumbDrawable = seekbar.getThumb();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			BlendModeColorFilter blendFilter = new BlendModeColorFilter(
				themeColor, BlendMode.SRC_IN);

			progressDrawable.setColorFilter(blendFilter);
			thumbDrawable.setColorFilter(blendFilter);
		}
		else
		{
			progressDrawable.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
			thumbDrawable.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
		}
	}

}
