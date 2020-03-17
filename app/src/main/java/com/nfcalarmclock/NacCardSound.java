package com.nfcalarmclock;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Sound for an alarm card.
 */
public class NacCardSound
{

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Sound.
	 */
	private LinearLayout mSoundParent;

	/**
	 * Sound.
	 */
	private TextView mSound;

	/**
	 * Volume.
	 */
	private SeekBar mVolume;

	/**
	 * Volume icon.
	 */
	private ImageView mVolumeIcon;

	/**
	 * Media source settings.
	 */
	private ImageView mAudioSourceIcon;

	/**
	 * Card measurement.
	 */
	private NacCardMeasure mMeasure;

	/**
	 */
	public NacCardSound(Context context, View root, NacCardMeasure measure)
	{
		this.mContext = context;
		this.mSoundParent = (LinearLayout) root.findViewById(R.id.nac_sound);
		this.mSound = (TextView) root.findViewById(R.id.sound_name);
		this.mVolume = (SeekBar) root.findViewById(R.id.nac_volume_slider);
		this.mVolumeIcon = (ImageView) root.findViewById(R.id.nac_volume_icon);
		this.mAudioSourceIcon = (ImageView) root.findViewById(R.id.nac_volume_settings);
		this.mMeasure = measure;
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The card padding.
	 */
	private int getCardPadding()
	{
		return this.mMeasure.getCardPadding();
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The screen width.
	 */
	private int getScreenWidth()
	{
		return this.mMeasure.getScreenWidth();
	}

	/**
	 * @return The vibrate width.
	 */
	private int getVibrateWidth()
	{
		return this.mMeasure.getVibrateWidth();
	}

	/**
	 * Initialize the sound.
	 */
	public void init(NacSharedPreferences shared, NacAlarm alarm)
	{
		this.mAlarm = alarm;

		this.set(shared);
	}

	/**
	 * Set the volume and sound to play when the alarm goes off.
	 */
	public void set(NacSharedPreferences shared)
	{
		this.setSound();
		this.setVolume();
		this.setVolumeIcon();
		this.setVolumeColor(shared);
	}

	/**
	 * Set the listeners.
	 */
	public void setListener(Object listener)
	{
		this.mSoundParent.setOnClickListener((View.OnClickListener)listener);
		this.mVolume.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener)listener);
		this.mAudioSourceIcon.setOnClickListener((View.OnClickListener)listener);
	}

	/**
	 * Set the sound.
	 */
	public void setSound()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		String path = alarm.getMediaPath();
		String message = NacSharedPreferences.getMediaMessage(context, path);
		float alpha = ((path != null) && !path.isEmpty()) ? 1.0f : 0.5f;

		this.mSound.setText(message);
		this.mSound.setAlpha(alpha);
	}

	/**
	 * Set the volume.
	 */
	public void setVolume()
	{
		NacAlarm alarm = this.getAlarm();

		//this.mVolume.incrementProgressBy(10);
		this.mVolume.setProgress(alarm.getVolume());
	}

	/**
	 * Set the volume color.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.Q)
	@SuppressLint("NewApi")
	public void setVolumeColor(NacSharedPreferences shared)
	{
		int themeColor = shared.getThemeColor();
		Drawable progressDrawable = this.mVolume.getProgressDrawable();
		Drawable thumbDrawable = this.mVolume.getThumb();

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

	/**
	 * Set the volume icon.
	 */
	public void setVolumeIcon()
	{
		NacAlarm alarm = this.getAlarm();
		int progress = alarm.getVolume();

		if (progress == 0)
		{
			this.mVolumeIcon.setImageResource(R.mipmap.volume_off);
		}
		else if ((progress > 0) && (progress <= 33))
		{
			this.mVolumeIcon.setImageResource(R.mipmap.volume_low);
		}
		else if ((progress > 33) && (progress <= 66))
		{
			this.mVolumeIcon.setImageResource(R.mipmap.volume_med);
		}
		else
		{
			this.mVolumeIcon.setImageResource(R.mipmap.volume_high);
		}
	}

	/**
	 * Show the media settings dialog.
	 */
	public void showAudioSourceDialog(NacDialog.OnDismissListener listener)
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		NacAudioSourceDialog dialog = new NacAudioSourceDialog();

		dialog.build(context);
		dialog.saveData(alarm.getAudioSource());
		dialog.addOnDismissListener(listener);
		dialog.show();
	}

	/**
	 * Start the sound activity.
	 */
	public void startActivity()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		Intent intent = NacIntent.toIntent(context, NacMediaActivity.class,
			alarm);

		context.startActivity(intent);
	}

}
