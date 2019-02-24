package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputType;
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
	implements NacDialog.OnDismissListener,TextView.OnEditorActionListener,View.OnTouchListener
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

		this.addDismissListener(this);
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
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(Context context, View root)
	{
		this.mColorPicker = (NacColorPicker) root.findViewById(R.id.color_picker);
		this.mColorExample = (ImageView) root.findViewById(R.id.color_example);
		this.mEditText = (EditText) root.findViewById(R.id.color_value);

		Object data = this.getData();
		String name = (data != null) ? (String) data : "ffffff";

		this.mEditText.setText(name);
		//this.mEditText.selectAll();
		this.mEditText.setOnEditorActionListener(this);
		this.mEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		this.mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		this.mColorPicker.setOnTouchListener(this);
		this.mColorExample.setBackgroundColor(Color.parseColor("#"+name));
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		v.onTouchEvent(event);

		float[] hsv = this.mColorPicker.getHSV();
		int color = Color.HSVToColor(hsv);
		NacUtility.printf("Dialog onTouch! HSV = (%f, %f, %f)", hsv[0], hsv[1], hsv[2]);
		this.mColorExample.setBackgroundColor(color);
		this.mEditText.setText(Integer.toHexString(color));

		return true;
	}

	/**
	 * Save the alarm name as the dialog data.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		String name = this.mEditText.getText().toString();

		dialog.saveData(name);

		return true;
	}

	/**
	 * Close the keyboard when the user hits enter.
	 */
	@Override
	public boolean onEditorAction(TextView tv, int action, KeyEvent event)
	{
		if ((event == null) && (action == EditorInfo.IME_ACTION_DONE))
		{
			closeKeyboard(tv);
			//this.dismiss();

			return true;
		}

		return false;
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

}
