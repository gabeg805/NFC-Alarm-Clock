package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

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
	 private NacImageTextButton mSound;

	/**
	 * Volume.
	 */
	private SeekBar mVolume;

	/**
	 * Media source settings.
	 */
	private ImageView mMediaSource;

	/**
	 * Card measurement.
	 */
	private NacCardMeasure mMeasure;

	/**
	 */
	public NacCardSound(Context context, View root, NacCardMeasure measure)
	{
		this.mContext = context;
		this.mSound = (NacImageTextButton) root.findViewById(R.id.nac_sound);
		this.mVolume = (SeekBar) root.findViewById(R.id.nac_volume_slider);
		this.mMediaSource = (ImageView) root.findViewById(R.id.nac_volume_settings);
		this.mMeasure = measure;
	}

	/**
	 * Ellipsize the sound.
	 */
	private void ellipsize()
	{
		RelativeLayout.LayoutParams params = this.getLayoutParams();
		params.width = this.getMaxWidth();

		this.mSound.setLayoutParams(params);

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
	 * @return The layout params.
	 */
	private RelativeLayout.LayoutParams getLayoutParams()
	{
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
			this.mSound.getLayoutParams();
		
		return (params != null) ? params : new RelativeLayout.LayoutParams(
			RelativeLayout.LayoutParams.WRAP_CONTENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT);
	}

	/**
	 * @return The max width of the sound name before it gets ellipsized.
	 */
	private int getMaxWidth()
	{
		int screenWidth = this.getScreenWidth();
		int padding = this.getCardPadding();
		int vibrate = this.getVibrateWidth();
		int textsize = this.mSound.getTextSize();

		return screenWidth - vibrate - padding - textsize;
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
	public void init(NacAlarm alarm)
	{
		this.mAlarm = alarm;

		this.set();
	}

	/**
	 * Set the volume and sound to play when the alarm goes off.
	 */
	public void set()
	{
		this.setSound();
		this.setVolume();
	}

	/**
	 * Set the listeners.
	 */
	public void setListener(Object listener)
	{
		this.mSound.setOnClickListener((View.OnClickListener)listener);
		this.mVolume.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener)listener);
		this.mMediaSource.setOnClickListener((View.OnClickListener)listener);
	}

	/**
	 * Set the sound.
	 */
	public void setSound()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		String path = alarm.getSoundPath();
		String message = NacSharedPreferences.getSoundMessage(context, path);
		boolean focus = (!path.isEmpty());

		this.mSound.setText(message);
		this.mSound.setFocus(focus);
		this.mSound.getTextView().setMaxLines(1);
		this.mSound.getTextView().setEllipsize(TextUtils.TruncateAt.END);
		this.ellipsize();
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
