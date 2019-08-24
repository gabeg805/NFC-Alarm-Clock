package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

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
	 * Card padding.
	 */
	private int mCardPadding;

	/**
	 * Vibrate width.
	 */
	private int mVibrateWidth;

	/**
	 */
	public NacCardSound(Context context, View root)
	{
		this.mContext = context;
		this.mSound = (NacImageTextButton) root.findViewById(R.id.nac_sound);

		RelativeLayout header = root.findViewById(R.id.nac_header);
		CheckBox vibrate = (CheckBox) root.findViewById(R.id.nac_vibrate);
		this.mCardPadding = header.getPaddingStart() + header.getPaddingEnd();
		this.mVibrateWidth = NacUtility.getWidth(vibrate);
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
		return this.mCardPadding;
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
		int vibrate = this.mVibrateWidth;
		int textsize = this.mSound.getTextSize();

		return screenWidth - vibrate - padding - textsize;
	}

	/**
	 * @return The context resources.
	 */
	public Resources getResources()
	{
		return this.getContext().getResources();
	}

	/**
	 * @return The screen width.
	 */
	private int getScreenWidth()
	{
		return this.getResources().getDisplayMetrics().widthPixels;
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
	 * Set the sound.
	 */
	public void set()
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
	 * Set the on click listener.
	 */
	public void setOnClickListener(View.OnClickListener listener)
	{
		this.mSound.setOnClickListener((View.OnClickListener)listener);
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
