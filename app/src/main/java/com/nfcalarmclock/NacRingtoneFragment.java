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
	 */
	public NacRingtoneFragment()
	{
		super();

		this.mRadioGroup = null;
	}

	/**
	 * @return The radio button padding.
	 */
	private int[] getPadding(Context context)
	{
		Resources res = context.getResources();
		int top = (int) res.getDimension(R.dimen.pt_frg_sound);
		int bottom = (int) res.getDimension(R.dimen.pb_frg_sound);
		int start = (int) res.getDimension(R.dimen.sp_frg_sound);
		int end = 0;

		return new int[] {start, top, end, bottom};
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
		Bundle bundle = NacAlarmParcel.toBundle(alarm);

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
			NacMediaPlayer player = this.getMediaPlayer();
			String path = (String) view.getTag();

			this.setMedia(path);
			player.reset();
			player.play(path);
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
		NacAlarm alarm = this.getAlarm();
		List<NacSound> ringtones = NacSound.getRingtones(context);
		String path = (alarm != null) ? alarm.getSound() : "";
		int[] padding = this.getPadding(context);
		int textSize = (int) context.getResources().getDimension(
			R.dimen.tsz_frg_sound);

		for(int i=0; i < ringtones.size(); i++)
		{
			RadioButton button  = new RadioButton(context);
			RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
			NacSound sound = ringtones.get(i);

			this.mRadioGroup.addView(button);
			button.setText(sound.getName());
			button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			button.setTag(sound.getPath());
			button.setLayoutParams(params);
			button.setPadding(padding[0], padding[1], padding[2], padding[3]);
			button.setOnClickListener(this);

			if (!path.isEmpty() && path.equals(sound.getPath()))
			{
				button.setChecked(true);
			}
		}
	}

}
