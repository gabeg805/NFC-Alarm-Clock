package com.nfcalarmclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.fragment.app.Fragment;
import java.util.Map;
import java.util.TreeMap;

/**
 * Display a dialog that shows a list of alarm ringtones.
 */
public class NacRingtoneFragment
	extends NacMediaFragment
	implements View.OnClickListener
{

	/**
	 * Radio group.
	 */
	private RadioGroup mRadioGroup;

	/**
	 * Radio button padding.
	 */
	private int[] mPadding;

	/**
	 * Radio button text size.
	 */
	private int mTextSize;

	/**
	 */
	public NacRingtoneFragment()
	{
		super();

		this.mRadioGroup = null;
		this.mPadding = new int[4];
		this.mTextSize = 0;
	}

	/**
	 * Create a radio button.
	 */
	private RadioButton addRadioButton(String title, String path)
	{
		Context context = getContext();
		RadioButton button  = new RadioButton(context);
		RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT);
		int[] padding = this.getPadding();
		int textSize = this.getTextSize();

		this.mRadioGroup.addView(button);
		button.setText(title);
		button.setTag(path);
		button.setLayoutParams(params);
		button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		button.setPadding(padding[0], padding[1], padding[2], padding[3]);
		button.setOnClickListener(this);
		return button;
	}

	/**
	 * @see addRadioButton
	 */
	private void addRandomRadioButton()
	{
	}

	/**
	 * @return The radio button padding.
	 */
	private int[] getPadding()
	{
		return this.mPadding;
	}

	/**
	 * @return The radio group.
	 */
	private RadioGroup getRadioGroup()
	{
		return this.mRadioGroup;
	}

	/**
	 * @return The text size.
	 */
	private int getTextSize()
	{
		return this.mTextSize;
	}

	/**
	 * Create a new instance of this fragment.
	 */
	public static Fragment newInstance(NacAlarm alarm)
	{
		Fragment fragment = new NacRingtoneFragment();
		Bundle bundle = NacBundle.toBundle(alarm);

		fragment.setArguments(bundle);
		return fragment;
	}

	/**
	 * Create a new instance of this fragment.
	 */
	public static Fragment newInstance(String media)
	{
		Fragment fragment = new NacRingtoneFragment();
		Bundle bundle = NacBundle.toBundle(media);

		fragment.setArguments(bundle);
		return fragment;
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		super.onClick(view);

		Context context = getContext();
		int id = view.getId();

		if (id == R.id.clear)
		{
			this.getRadioGroup().clearCheck();
		}
		else if (!this.isActionButton(id))
		{
			String path = (String) view.getTag();
			Uri uri = NacMedia.toUri(path);

			if (this.safePlay(uri, true) < 0)
			{
				NacSharedConstants cons = new NacSharedConstants(context);
				NacUtility.printf("Unable to play ringtone : %s", path);
				NacUtility.toast(context, cons.getErrorMessagePlayAudio());
			}
		}
	}

	/**
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.frg_ringtone, container, false);
	}

	/**
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		setupPadding();
		setupTextSize();
		setupActionButtons(view);
		setupRadioButtons(view);
	}

	/**
	 * Set the radio button's color state list.
	 */
	public void setRadioButtonColor(NacSharedPreferences shared,
		RadioButton radioButton)
	{
		int[] colors = new int[] { shared.getThemeColor(), Color.GRAY };

		int[][] states = new int[][] {
			new int[] {  android.R.attr.state_checked },
			new int[] { -android.R.attr.state_checked } };
		ColorStateList stateList = new ColorStateList(states, colors);

		radioButton.setButtonTintList(stateList);
	}

	/**
	 * Setup radio buttons.
	 */
	private void setupRadioButtons(View root)
	{
		this.mRadioGroup = (RadioGroup) root.findViewById(R.id.radio_group);
		Context context = getContext();
		TreeMap<String,String> ringtones = NacMedia.getRingtones(context);
		NacSharedPreferences shared = new NacSharedPreferences(context);

		for (Map.Entry<String,String> entry : ringtones.entrySet())
		{
			String title = entry.getKey();
			String path = entry.getValue();
			RadioButton button = this.addRadioButton(title, path);

			this.setRadioButtonColor(shared, button);

			if (this.isSelectedPath(path))
			{
				button.setChecked(true);
			}
		}
	}

	/**
	 * Setup the padding (left, top, right, bottom).
	 */
	private void setupPadding()
	{
		Context context = getContext();
		Resources res = context.getResources();
		int main = (int) res.getDimension(R.dimen.sp_main);
		this.mPadding[0] = main;
		this.mPadding[1] = main;
		this.mPadding[2] = 0;
		this.mPadding[3] = main;
	}

	/**
	 * Setup the text size.
	 */
	private void setupTextSize()
	{
		Context context = getContext();
		Resources res = context.getResources();
		this.mTextSize = (int) res.getDimension(R.dimen.tsz_main);
	}

}
