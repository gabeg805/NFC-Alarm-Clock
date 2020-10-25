package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

/**
 * The dialog class that will handle saving the name of the alarm.
 */
public class NacNameDialog
	extends NacDialog
	implements TextView.OnEditorActionListener,
		NacDialog.OnDismissListener,
		NacDialog.OnShowListener
{

	/**
	 */
	public NacNameDialog()
	{
		super(R.layout.dlg_alarm_name);

		addOnDismissListener(this);
		addOnShowListener(this);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		NacSharedConstants cons = new NacSharedConstants(context);

		//builder.setTitle(cons.getTitleName());
		setPositiveButton(cons.getActionOk());
		setNegativeButton(cons.getActionCancel());
	}

	/**
	 * Save the alarm name as the dialog data.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		View root = dialog.getRoot();
		TextInputEditText editText = root.findViewById(R.id.name_entry);
		String name = editText.getText().toString();

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
			dismiss();
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
		String name = this.getDataString();
		int theme = shared.getThemeColor();
		int blendedTheme = ColorUtils.blendARGB(theme, Color.TRANSPARENT, 0.1f);
		ColorStateList colorStateList = ColorStateList.valueOf(blendedTheme);
		TextInputLayout editBox = root.findViewById(R.id.name_box);
		TextInputEditText editText = root.findViewById(R.id.name_entry);

		editText.setText(name);
		editText.selectAll();
		editText.setOnEditorActionListener(this);
		editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		//editText.setBackgroundTintList(ColorStateList.valueOf(themeColor));
		editBox.setBoxStrokeColor(blendedTheme);
		editBox.setHintTextColor(colorStateList);
		showKeyboard();
		scale(0.85, 0.5, false, true);
	}

}
