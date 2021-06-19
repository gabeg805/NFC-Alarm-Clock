package com.nfcalarmclock.audio.media;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.nfcalarmclock.R;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.system.NacBundle;

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
	 */
	public NacRingtoneFragment()
	{
		super();

		this.mRadioGroup = null;
	}

	/**
	 * Create a radio button.
	 */
	private RadioButton addRadioButton(String title, String path)
	{
		Context context = getContext();
		LayoutInflater inflater = LayoutInflater.from(context);
		RadioGroup group = this.getRadioGroup();
		View view = inflater.inflate(R.layout.radio_button_ringtone, group, true);
		RadioButton button = view.findViewById(R.id.radio_button_ringtone);
		int id = View.generateViewId();

		button.setId(id);
		button.setText(title);
		button.setTag(path);
		button.setOnClickListener(this);
		return button;
	}

	/**
	 * @return The radio group.
	 */
	private RadioGroup getRadioGroup()
	{
		return this.mRadioGroup;
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
		int id = view.getId();

		if (id == R.id.clear)
		{
			this.getRadioGroup().clearCheck();
		}
		else if (!this.isActionButton(id))
		{
			String path = (String) view.getTag();
			Uri uri = NacMedia.toUri(path);

			if (this.safePlay(uri) < 0)
			{
				this.showErrorPlayingAudio();
			}

			return;
		}

		super.onClick(view);
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
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
	{
		setupActionButtons(view);
		this.setupRadioButtons(view);
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
		this.mRadioGroup = root.findViewById(R.id.radio_group);
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

}
