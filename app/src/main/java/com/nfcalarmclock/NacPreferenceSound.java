package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

/**
 * Preference that displays the sound prompt dialog.
 */
public class NacPreferenceSound
	extends Preference
	implements Preference.OnPreferenceClickListener
{

	/**
	 * Path of the sound.
	 */
	protected String mValue;

	/**
	 */
	public NacPreferenceSound(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceSound(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceSound(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference);
		setOnPreferenceClickListener(this);
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		Context context = getContext();
		String path = this.mValue;
		String name = NacSound.getName(context, path);

		return (!name.isEmpty()) ? name
			: NacSharedPreferences.DEFAULT_SOUND_SUMMARY;
	}

	/**
	 * Set the summary text.
	 */
	@Override
	protected void onBindView(View v)
	{
		super.onBindView(v);
		setSummary(this.getSummary());
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (String) a.getString(index);
	}

	/**
	 * Capture which item in the list was selected.
	 */
	//@Override
	//public void onItemClick(String path, String name)
	//{
	//	if (path.isEmpty())
	//	{
	//		return;
	//	}

	//	this.mValue = path;

	//	setSummary(this.getSummary());
	//	persistString(this.mValue);
	//}

	/**
	 * When the preference is clicked, display the dialog.
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		NacUtility.printf("Sound prompt display!");
		Context context = getContext();
		NacSound sound = new NacSound(context, this.mValue);

		sound.setData(getKey());

		Intent intent = NacIntent.toIntent(context, NacPagerFragment.class,
			sound);

		context.startActivity(intent);

		return true;
	}

	/**
	 * Set the initial preference value.
	 */
	@Override
	protected void onSetInitialValue(boolean restore, Object defval)
	{
		if (restore)
		{
			this.mValue = getPersistedString(this.mValue);
		}
		else
		{
			this.mValue = (String) defval;

			persistString(this.mValue);
		}
	}

}
