package com.nfcalarmclock.volume;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.google.android.material.button.MaterialButton;
import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedDefaults;
import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 * Preference to choose the default volume and audio source.
 */
@SuppressWarnings("RedundantSuppression")
public class NacVolumePreference
	extends Preference
	implements View.OnClickListener,
		SeekBar.OnSeekBarChangeListener
{

	/**
	 * Listener for when the audio options button is clicked.
	 */
	public interface OnAudioOptionsClickedListener
	{
		public void onAudioOptionsClicked();
	}

	/**
	 * Volume level.
	 */
	protected int mValue;

	/**
	 * Seekbar.
	 */
	protected SeekBar mVolumeSeekBar;

	/**
	 * Audio options button.
	 */
	protected MaterialButton mAudioOptionsButton;

	/**
	 * Shared preferences.
	 */
	protected NacSharedPreferences mSharedPreferences;

	/**
	 * Audio options button clicked listner.
	 */
	protected OnAudioOptionsClickedListener mOnAudioOptionsClickedListener;

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
	 * Call the listener to when the audio options button is clicked.
	 */
	public void callOnAudioOptionsClickedListener()
	{
		OnAudioOptionsClickedListener listener = this.getOnAudioOptionsClickedListener();

		if (listener != null)
		{
			listener.onAudioOptionsClicked();
		}
	}

	/**
	 * @return Audio options button.
	 */
	public MaterialButton getAudioOptionsButton()
	{
		return this.mAudioOptionsButton;
	}

	/**
	 * @return Listener to call when the audio options button is clicked.
	 */
	public OnAudioOptionsClickedListener getOnAudioOptionsClickedListener()
	{
		return this.mOnAudioOptionsClickedListener;
	}

	/**
	 * @return Volume seekbar.
	 */
	public SeekBar getVolumeSeekBar()
	{
		return this.mVolumeSeekBar;
	}

	/**
	 * @return Shared preferences.
	 */
	private NacSharedPreferences getNacSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 */
	@Override
	public void onAttached()
	{
		super.onAttached();

		Context context = getContext();
		this.mSharedPreferences = new NacSharedPreferences(context);
	}

	/**
	 */
	@Override
	public void onBindViewHolder(PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);

		SeekBar seekbar = (SeekBar) holder.findViewById(R.id.volume_slider);
		MaterialButton audioOptions = (MaterialButton) holder.findViewById(R.id.widget);
		this.mVolumeSeekBar = seekbar;
		this.mAudioOptionsButton = audioOptions;

		seekbar.setProgress(this.mValue);
		seekbar.setOnSeekBarChangeListener(this);
		audioOptions.setOnClickListener(this);
		this.setSeekBarColor();
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		this.callOnAudioOptionsClickedListener();
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		Context context = getContext();
		NacSharedDefaults defs = new NacSharedDefaults(context);

		return a.getInteger(index, defs.getVolume());
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
	 * Set the listener for when the audio options button is clicked.
	 *
	 * @param  listener  Listener for when the audio options button is clicked.
	 */
	public void setOnAudioOptionsClickedListener(
		OnAudioOptionsClickedListener listener)
	{
		this.mOnAudioOptionsClickedListener = listener;
	}

	/**
	 * Set the volume seekbar color.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.Q)
	@SuppressLint("NewApi")
	public void setSeekBarColor()
	{
		NacSharedPreferences shared = this.getNacSharedPreferences();
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
