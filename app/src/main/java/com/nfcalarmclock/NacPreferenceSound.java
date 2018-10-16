package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.support.v4.content.ContextCompat;
import android.graphics.Color;
import android.text.style.ForegroundColorSpan;
import android.text.Spannable;
import android.text.SpannableString;

/**
 */
public class NacPreferenceSound
	extends Preference
	implements Preference.OnPreferenceClickListener,NacSoundDialog.OnItemClickListener
{

	/**
	 * Name of the sound.
	 */
	protected String mValueName;

	/**
	 * Path of the sound.
	 */
	protected String mValuePath;

	/**
	 * Default for the sound name.
	 */
	protected static final String mDefaultName = "None";

	/**
	 * Default for the sound path.
	 */
	protected static final String mDefaultPath = "";

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
		setLayoutResource(R.layout.pref_sound);
		setOnPreferenceClickListener(this);

		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getContext());
		this.mValueName = shared.getString("pref_sound_name", this.mDefaultName);
		this.mValuePath = shared.getString("pref_sound_path", this.mDefaultPath);
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		return this.mValueName;
	}

	/**
	 * Set the summary text.
	 */
	@Override
	protected void onBindView(View v)
	{
		super.onBindView(v);
		this.setSummary(this.mValueName);
	}

	/**
	 * Capture which item in the list was selected.
	 */
	@Override
	public void onItemClick(NacSound sound)
	{
		String path = sound.path;
		String name = sound.name;

		if (path.isEmpty() || name.isEmpty())
		{
			return;
		}

		this.mValueName = name;
		this.mValuePath = path;
		SharedPreferences.Editor editor = getEditor();

		this.setSummary(this.mValueName);
		editor.putString("pref_sound_name", this.mValueName);
		editor.putString("pref_sound_path", this.mValuePath);
		editor.apply();
	}

	/**
	 * When the preference is clicked, display the dialog.
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		Context context = getContext();
		NacSoundPromptDialog dialog = new NacSoundPromptDialog();

		dialog.build(context, R.layout.dlg_sound_prompt);
		dialog.setOnItemClickListener(this);
		dialog.show();

		return true;
	}

}

