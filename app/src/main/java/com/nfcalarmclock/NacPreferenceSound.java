package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.SeekBar;

import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;

public class NacPreferenceSound
	extends Preference
	implements Preference.OnPreferenceClickListener,NacCardSoundPromptDialog.OnItemSelectedListener,SeekBar.OnSeekBarChangeListener
{

	/**
	 * @brief Day of week buttons.
	 */
	private NacCardSoundPromptDialog mDialog;

	/**
	 * @brief Text view containing the name of the sound to play.
	 */
	private TextView mTextView;
	private TextView mSubtitleView;
	private ImageView mImageView;
	private SeekBar mSeekBar;

	/**
	 * @brief Name of sound to play.
	 */
	private String mName;

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
		this.setOnPreferenceClickListener(this);
	}

	/**
	 */
	@Override
	protected View onCreateView(ViewGroup parent)
	{
		super.onCreateView(parent);

		Context context = getContext();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.pref_sound, parent, false);
	}

	/**
	 */
	@Override
	protected void onBindView(View v)
	{
		super.onBindView(v);

		this.mTextView = (TextView) v.findViewById(R.id.widget);
		this.mSubtitleView = (TextView) v.findViewById(android.R.id.summary);
		this.mImageView = (ImageView) v.findViewById(R.id.icon);
		this.mSeekBar = (SeekBar) v.findViewById(R.id.volume);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		AppCompatActivity act = (AppCompatActivity) getContext();

		act.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		int height = displayMetrics.heightPixels;
		int width = displayMetrics.widthPixels;
		LayoutParams params = new LayoutParams((int)(width*2.0/3.0), -1);

		this.mTextView.setText(this.mName);
		this.mTextView.setMaxWidth(width / 2);
		this.mSubtitleView.setMaxWidth(width / 2);
		this.mSeekBar.setLayoutParams(params);
		this.mSeekBar.setKeyProgressIncrement(10);
		this.mSeekBar.setOnSeekBarChangeListener(this);
	}

	/**
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (String) a.getString(index);
	}

	/**
	 */
	@Override
	protected void onSetInitialValue(boolean restore, Object defval)
	{
		if (restore)
		{
			this.mName = getPersistedString(this.mName);
		}
		else
		{
			this.mName = (String) defval;

			persistString(this.mName);
		}
	}

	/**
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		this.mDialog = new NacCardSoundPromptDialog(getContext());

		this.mDialog.setOnItemSelectedListener(this);
		this.mDialog.show();
		return true;
	}

	/**
	 */
	@Override
	public void onItemSelected(NacSound sound)
	{
		NacUtility.printf("Item has been selected!!!");

		String path = sound.path;
		String name = sound.name;

		if (path.isEmpty() || name.isEmpty())
		{
			return;
		}

		NacUtility.printf("Sound : %s", path);
		this.mName = name;

		persistString(this.mName);
		this.mTextView.setText(this.mName);
	}

	@Override
	public void onProgressChanged(SeekBar bar, int progress, boolean from)
	{
		NacUtility.printf("Progress : %d", progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar bar)
	{
		NacUtility.printf("Start tracking touch");
	}

	@Override
	public void onStopTrackingTouch(SeekBar bar)
	{
		NacUtility.printf("Stop tracking touch");
	}

}

