package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Handle displaying the color picker dialog.
 */
public class NacColorPickerDialog
	extends NacDialog
	implements TextView.OnEditorActionListener,
		TextWatcher,
		View.OnTouchListener,
		NacDialog.OnShowListener
{

	/**
	 * Color picker.
	 */
	private NacColorPicker mColorPicker;

	/**
	 * Color example.
	 */
	private ImageView mColorExample;

	/**
	 * Color hex value.
	 */
	private EditText mEditText;

	/**
	 */
	public NacColorPickerDialog()
	{
		super(R.layout.dlg_color_picker);
		addOnShowListener(this);
	}

	/**
	 * Ensure the text always starts with "#".
	 *
	 * Note: This is required to implement TextWatcher.
	 */
	@Override
	public void afterTextChanged(Editable s)
	{
		if (!s.toString().startsWith("#"))
		{
			s.insert(0, "#");
		}
	}

	/**
	 * Note: This is required to implement TextWatcher.
	 */
	@Override
	public void beforeTextChanged(CharSequence seq, int start, int count, int after)
	{
	}

	/**
	 * @return True if can parse the color, and False otherwise.
	 */
	public boolean canParseColor(String name)
	{
		try
		{
			int color = Color.parseColor(name);
			return true;
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}
	}

	/**
	 * @return The color of the edit text box.
	 */
	public int getColor()
	{
		return this.mColorPicker.getColor();
	}

	/**
	 * @return The EditText color.
	 */
	public String getEditTextColor()
	{
		return this.mEditText.getText().toString();
	}

	/**
	 * @return The last chosen color of the dialog.
	 */
	public String getHexColor()
	{
		return this.mColorPicker.getHexColor();
	}

	/**
	 * Check if valid hex string was input into the EditText.
	 */
	public boolean isHexString(String name)
	{
		for (int i=1; i < name.length(); i++)
		{
			if (Character.digit(name.charAt(i), 16) == -1)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		NacSharedConstants cons = new NacSharedConstants(context);

		builder.setTitle(cons.getTitleColor());
		setPositiveButton(cons.getActionOk());
		setNegativeButton(cons.getActionCancel());
	}

	/**
	 * Close the keyboard when the user hits enter.
	 */
	@Override
	public boolean onEditorAction(TextView tv, int action, KeyEvent event)
	{
		//if ((event == null) && (action == EditorInfo.IME_ACTION_DONE))
		if ((event != null) || (action != EditorInfo.IME_ACTION_DONE))
		{
			return false;
		}

		Context context = tv.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		String name = this.getEditTextColor();

		if (!this.isHexString(name) || !this.canParseColor(name))
		{
			NacUtility.quickToast(context, cons.getErrorMessageSelectColor());
			return false;
		}

		int color = Color.parseColor(name);
		this.mColorPicker.setColor(color);
		this.mColorExample.setColorFilter(color, PorterDuff.Mode.SRC);
		closeKeyboard();
		return true;
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		Context context = root.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		this.mColorPicker = root.findViewById(R.id.color_picker);
		this.mColorExample = root.findViewById(R.id.color_example);
		this.mEditText = root.findViewById(R.id.color_value);

		this.mEditText.addTextChangedListener(this);
		this.mEditText.setText(this.getHexColor());
		this.mEditText.setOnEditorActionListener(this);
		this.mEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		this.mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		this.mEditText.setBackgroundTintList(ColorStateList.valueOf(shared.getThemeColor()));
		this.mColorPicker.setOnTouchListener(this);
		this.mColorExample.setColorFilter(this.getColor(), PorterDuff.Mode.SRC);
	}

	/**
	 * Note: This is required to implement TextWatcher.
	 */
	@Override
	public void onTextChanged(CharSequence seq, int start, int before, int count)
	{
	}

	/**
	 * Capture touch events on the color picker.
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		view.onTouchEvent(event);

		int color = this.getColor();
		String hex = this.getHexColor();

		this.mColorExample.setColorFilter(color, PorterDuff.Mode.SRC);
		this.mEditText.setText(hex);

		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			view.performClick();
		}

		return true;
	}

	/**
	 * Set the color.
	 */
	public void setColor(int color)
	{
		this.mColorPicker.setColor(color);
		this.mColorExample.setColorFilter(color, PorterDuff.Mode.SRC);

		String hex = this.mColorPicker.getHexColor();

		this.mEditText.setText(hex);
	}

}
