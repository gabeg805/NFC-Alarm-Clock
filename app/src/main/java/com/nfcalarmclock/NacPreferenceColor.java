package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;;

/**
 * Preference that allows a user to select a color.
 */
public class NacPreferenceColor
	extends Preference
	implements Preference.OnPreferenceClickListener,
		NacDialog.OnDismissListener,
		NacDialog.OnNeutralActionListener
{

	/**
	 * Color image view.
	 */
	protected ImageView mImageView;

	/**
	 * Color value.
	 */
	protected int mValue;

	/**
	 * Default constant value for the object.
	 */
	protected int mDefault;

	/**
	 */
	public NacPreferenceColor(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceColor(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceColor(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference_widget);
		setOnPreferenceClickListener(this);
	}

	/**
	 * Setup the checkbox and summary text.
	 */
	@Override
	protected void onBindView(View v)
	{
		super.onBindView(v);

		this.mImageView = (ImageView) v.findViewById(R.id.widget);

		this.mImageView.setColorFilter(this.mValue, PorterDuff.Mode.SRC);
	}

	/**
	 * Save color when the dialog is dismissed.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		this.mValue = ((NacColorPickerDialog)dialog).getColor();

		this.mImageView.setColorFilter(this.mValue, PorterDuff.Mode.SRC);
		persistInt(this.mValue);
		callChangeListener(this.mValue);

		return true;
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (this.mDefault = (Integer) a.getInteger(index, Color.WHITE));
	}

	/**
	 * Set the default color when the neutral button is pressed.
	 */
	@Override
	public boolean onNeutralActionDialog(NacDialog dialog)
	{
		((NacColorPickerDialog)dialog).setColor(this.mDefault);

		return true;
	}

	/**
	 * Allow users to select the whole preference to change the checkbox.
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		NacColorPickerDialog dialog = new NacColorPickerDialog();
		Context context = this.mImageView.getContext();

		dialog.addOnDismissListener(this);
		dialog.addOnNeutralActionListener(this);
		dialog.build(context, R.layout.dlg_color_picker);
		dialog.setNeutralButton("Default");
		dialog.show();
		dialog.scale(0.75, 0.85, false, true);
		dialog.setColor(this.mValue);

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
			this.mValue = getPersistedInt(this.mValue);
		}
		else
		{
			this.mValue = (Integer) defval;

			persistInt(this.mValue);
		}
	}

}
