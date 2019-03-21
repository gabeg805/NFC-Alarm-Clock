package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Handle displaying the color picker dialog.
 */
public class NacColorPickerDialog
	extends NacDialog
	implements TextView.OnEditorActionListener,View.OnTouchListener,TextWatcher,NacDialog.OnShowListener
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
		super();

		this.addOnShowListener(this);
	}

	/**
	 * Ensure the text always starts with "#".
	 *
	 * @note This is required to implement TextWatcher.
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
	 * @note This is required to implement TextWatcher.
	 */
	@Override
	public void beforeTextChanged(CharSequence seq, int start, int count, int after)
	{
	}

	/**
	 * Close the keyboard.
	 */
	private void closeKeyboard(TextView tv)
	{
		this.mEditText.clearFocus();

		Context context = tv.getContext();
		String name = Context.INPUT_METHOD_SERVICE;
		int flags = InputMethodManager.RESULT_UNCHANGED_SHOWN;
		Object service = context.getSystemService(name);
		InputMethodManager manager = (InputMethodManager) service;

		manager.hideSoftInputFromWindow(tv.getWindowToken(), flags);
	}

	/**
	 * @return The color of the edit text box.
	 */
	public int getColor()
	{
		return this.mColorPicker.getColor();
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
	public boolean isHexString()
	{
		String name = this.mEditText.getText().toString();
		//NacUtility.printf("Name : %s", name);

		for (int i=1; i < name.length(); i++)
		{
			//NacUtility.printf("Char : %c | Result : %d", name.charAt(i), Character.digit(name.charAt(i), 16));
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
		String title = "Choose Color";

		builder.setTitle(title);
		this.setPositiveButton("OK");
		this.setNegativeButton("Cancel");
	}

	/**
	 * Close the keyboard when the user hits enter.
	 */
	@Override
	public boolean onEditorAction(TextView tv, int action, KeyEvent event)
	{
		if ((event == null) && (action == EditorInfo.IME_ACTION_DONE))
		{
			if (!this.isHexString())
			{
				NacUtility.quickToast(tv.getContext(), "Invalid hex color");
				return false;
			}

			String name = this.mEditText.getText().toString();
			int color = Color.parseColor(name);

			closeKeyboard(tv);
			this.mColorPicker.setColor(color);
			this.mColorExample.setBackgroundColor(color);

			return true;
		}

		return false;
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		Context context = root.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		this.mColorPicker = (NacColorPicker) root.findViewById(R.id.color_picker);
		this.mColorExample = (ImageView) root.findViewById(R.id.color_example);
		this.mEditText = (EditText) root.findViewById(R.id.color_value);

		this.mEditText.addTextChangedListener(this);
		this.mEditText.setText(this.getHexColor());
		this.mEditText.setOnEditorActionListener(this);
		this.mEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		this.mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		this.mEditText.setBackgroundTintList(ColorStateList.valueOf(shared.getThemeColor()));
		this.mColorPicker.setOnTouchListener(this);
		this.mColorExample.setBackgroundColor(this.getColor());
	}

	/**
	 * @note This is required to implement TextWatcher.
	 */
	@Override
	public void onTextChanged(CharSequence seq, int start, int before, int count)
	{
	}

	/**
	 * Capture touch events on the color picker.
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		v.onTouchEvent(event);

		int color = this.getColor();
		String hex = this.getHexColor();

		this.mColorExample.setBackgroundColor(color);
		this.mEditText.setText(hex);

		return true;
	}

	/**
	 * Set the color.
	 */
	public void setColor(int color)
	{
		NacUtility.printf("Set color : %d", color);
		this.mColorPicker.setColor(color);
		this.mColorExample.setBackgroundColor(color);

		String hex = this.mColorPicker.getHexColor();

		this.mEditText.setText(hex);
	}

}
