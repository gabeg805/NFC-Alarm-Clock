package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.ArrayList;
import java.util.List;

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
	private void addRadioButton(NacSound sound)
	{
		Context context = getContext();
		RadioButton button  = new RadioButton(context);
		RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT);
		String path = getSoundPath();
		int[] padding = this.getPadding();
		int textSize = this.getTextSize();

		this.mRadioGroup.addView(button);
		button.setText(sound.getName());
		button.setTag(sound.getPath());
		button.setLayoutParams(params);
		button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		button.setPadding(padding[0], padding[1], padding[2], padding[3]);
		button.setOnClickListener(this);

		if (!path.isEmpty() && path.equals(sound.getPath()))
		{
			button.setChecked(true);
		}
	}

	/**
	 * @see addRadioButton
	 */
	private void addRandomRadioButton()
	{
		//NacSound sound = new NacSound(NacSound.TYPE_RINGTONE_RANDOM);

		//this.addRadioButton(sound);
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
	public static Fragment newInstance(NacSound sound)
	{
		Fragment fragment = new NacRingtoneFragment();
		Bundle bundle = NacBundle.toBundle(sound);

		fragment.setArguments(bundle);

		return fragment;
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		super.onClick(view);

		int id = view.getId();

		if (id == R.id.clear)
		{
			this.getRadioGroup().clearCheck();
		}
		else if (!this.isActionButton(id))
		{
			String path = (String) view.getTag();

			if (this.safePlay(path, true) < 0)
			{
				NacUtility.toast(getContext(), "Unable to play ringtone");
			}

			//NacMediaPlayer player = this.getMediaPlayer();
			//this.setMedia(path);
			//player.reset();
			//player.play(path, true);
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
	 * Setup radio buttons.
	 */
	private void setupRadioButtons(View root)
	{
		this.mRadioGroup = (RadioGroup) root.findViewById(R.id.radio_group);
		Context context = getContext();
		List<NacSound> ringtones = NacSound.getRingtones(context);

		this.addRandomRadioButton();

		for(int i=0; i < ringtones.size(); i++)
		{
			NacSound sound = ringtones.get(i);

			this.addRadioButton(sound);
		}
	}

	/**
	 * Setup the padding.
	 */
	private void setupPadding()
	{
		Context context = getContext();
		Resources res = context.getResources();
		this.mPadding[0] = (int) res.getDimension(R.dimen.sp_frg_sound);
		this.mPadding[1] = (int) res.getDimension(R.dimen.pt_frg_sound);
		this.mPadding[2] = 0;
		this.mPadding[3] = (int) res.getDimension(R.dimen.pb_frg_sound);
	}

	/**
	 * Setup the text size.
	 */
	private void setupTextSize()
	{
		Context context = getContext();
		Resources res = context.getResources();
		this.mTextSize = (int) res.getDimension(R.dimen.tsz_frg_sound);
	}

}
