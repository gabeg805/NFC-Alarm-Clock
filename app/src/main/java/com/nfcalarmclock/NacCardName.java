package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @brief The alarm name. Users can change the name upon clicking the view.
 */
public class NacCardName
    implements View.OnClickListener
{

    /**
     * @brief Context.
     */
     private Context mContext;

    /**
     * @brief Alarm.
     */
     private Alarm mAlarm;

    /**
     * @brief Name.
     */
     private ImageTextButton mName;

    /**
     * @brief Name dialog.
     */
     private NacCardNameDialog mNameDialog;

    /**
     * @brief Constructor.
     */
    public NacCardName(Context c, View r)
    {
        this.mContext = c;
        this.mNameDialog = new NacCardNameDialog();
        this.mName = (ImageTextButton) r.findViewById(R.id.nacName);
        this.mName.setOnClickListener(this);
    }

    /**
     * @brief Initialize the name.
     */
    public void init(Alarm alarm)
    {
		this.mAlarm = alarm;
        this.mName.setText(this.mAlarm.getName());
    }

    /**
     * @brief Display the dialog that allows users to set the name of thee alarm.
     */
    @Override
    public void onClick(View v)
    {
        this.mNameDialog.show();
    }

    /**
     * @brief The dialog class that will handle saving the name of the alarm.
     */
    public class NacCardNameDialog
        implements DialogInterface.OnClickListener,TextView.OnEditorActionListener
    {

        /**
         * @brief Dialog.
         */
        private AlertDialog mDialog = null;

        /**
         * @brief The EditText in the dialog.
         */
        private EditText mEditText = null;

        /**
         * @brief Show the dialog to set the alarm name.
         */
        public void show()
        {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.dlg_alarm_name, null);
            initDialog(v);
            initEditText(v);
        }

        /**
         * @Override Save the data that was entered in the dialog; namely, the
         *           alarm name.
         */
        public void saveData()
        {
            String text = mEditText.getText().toString();
            mAlarm.setName(text);
            mName.setText(text);
        }

        /**
         * @brief Setup the dialog's edit text.
         */
        private void initEditText(View v)
        {
            this.mEditText = (EditText) v.findViewById(R.id.nacNameInput);
            this.mEditText.setText(mAlarm.getName());
            this.mEditText.selectAll();
            this.mEditText.setOnEditorActionListener(this);
            this.mEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
            this.mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }

        /**
         * @brief Setup the dialog.
         */
        private void initDialog(View v)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(v);
            builder.setTitle("Set Alarm Name");
            builder.setPositiveButton("OK", this);
            builder.setNegativeButton("Cancel", this);
            this.mDialog = builder.show();
            this.mDialog.setCancelable(true);
            this.mDialog.setCanceledOnTouchOutside(true);
        }

        /**
         * @brief Close the keyboard.
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
         * @brief Handles click events on the Ok/Cancel buttons in the dialog.
         */
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
            case DialogInterface.BUTTON_POSITIVE:
                saveData();
                dialog.dismiss();
                return;
            case DialogInterface.BUTTON_NEGATIVE:
            default:
                dialog.cancel();
                return;
            }
        }

        /**
         * @brief Close the keyboard when the user hits enter.
         */
        @Override
        public boolean onEditorAction(TextView tv, int actionId,
                                      KeyEvent event)
        {
            if (event == null)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    closeKeyboard(tv);
                    saveData();
                    mDialog.dismiss();
                }
                else
                {
                    return false;
                }
            }
            else if (actionId == EditorInfo.IME_NULL)
            {
                if ((event.getAction() != KeyEvent.ACTION_DOWN)
                    && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                {
                    closeKeyboard(tv);
                    saveData();
                    mDialog.dismiss();
                }
            }
            else
            {
                return false;
            }
            return true;
        }

    }

}
