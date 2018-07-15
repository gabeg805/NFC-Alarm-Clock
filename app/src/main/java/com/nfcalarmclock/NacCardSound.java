package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief The sound to play when the alarm is activated. Users can change this
 *        by selecting the sound view.
 */
public class NacCardSound
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
     * @brief Sound.
     */
     private ImageTextButton mSound;

    /**
     * @brief Sound dialog.
     */
     private NacCardSoundDialog mSoundDialog;

    /**
     * @brief Result from ringtone mangaer activity.
     */
    public final static int NAC_CARD_SOUND_REQUEST = 99;

    /**
     * @brief Constructor.
     */
    public NacCardSound(Context c, View r)
    {
        this.mContext = c;
        this.mSoundDialog = new NacCardSoundDialog();
        this.mSound = (ImageTextButton) r.findViewById(R.id.nacSound);
        this.mSound.setOnClickListener(this);
    }

    /**
     * @brief Initialize the sound view.
     */
    public void init(Alarm alarm)
    {
		this.mAlarm = alarm;
        // this.mSound.setText(this.mCard.getAlarm().getSound());
    }

    // /**
    //  * @brief Display the dialog that allows users to select an alarm soun.
    //  */
    // @Override
    // public void onClick(View v)
    // {
    //     AppCompatActivity activity = (AppCompatActivity) mContext;
    //     Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
    //     Bundle bundle = new Bundle();
    //     intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
    //                     RingtoneManager.TYPE_NOTIFICATION);
    //     intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
    //                     "Select Alarm Sound");
    //     intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
    //                     false);
    //     intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
    //                     true);
    //     bundle.putShort("Card", 
    //     activity.startActivityForResult(intent, NAC_CARD_SOUND_REQUEST);
    // }

    /**
     * @brief Display the dialog that allows users to set the name of thee alarm.
     */
    @Override
    public void onClick(View v)
    {
        this.mSoundDialog.show();
    }

    /**
     * @brief The dialog class that will handle saving the name of the alarm.
     */
    public class NacCardSoundDialog
        implements DialogInterface.OnClickListener,CompoundButton.OnCheckedChangeListener
    {

        /**
         * @brief Dialog.
         */
        private AlertDialog mDialog = null;

        // /**
        //  * @brief The EditText in the dialog.
        //  */
        // private EditText mEditText = null;

        private List<String> mSoundTitles;
        private List<Uri> mSoundUris;
        private RingtoneManager mRingtoneManager;

        public NacCardSoundDialog()
        {
            this.mRingtoneManager = new RingtoneManager(mContext);
            this.mSoundTitles = new ArrayList<>();
            this.mSoundUris = new ArrayList<>();
            this.mRingtoneManager.setType(RingtoneManager.TYPE_ALARM);
            this.mRingtoneManager.setStopPreviousRingtone(true);
        }

        /**
         * @brief Show the dialog to set the alarm name.
         */
        public void show()
        {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.dlg_alarm_sound, null);
            initDialog(v);
            initSoundList();
            initSoundRadioButtons(v);
        }

        // /**
        //  * @Override Save the data that was entered in the dialog; namely, the
        //  *           alarm name.
        //  */
        // public void saveData()
        // {
        //     String text = mEditText.getText().toString();
        //     malarm.setName(text);
        //     mName.setText(text);
        // }

        /**
         * @brief Setup the dialog's list of sounds.
         */
        private void initSoundList()
        {
            Cursor cursor = this.mRingtoneManager.getCursor();
            while (cursor.moveToNext())
            {
                String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
                String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
                this.mSoundTitles.add(title);
                this.mSoundUris.add(Uri.parse(uri+"/"+id));
            }
        }

        /**
         * @brief Setup the dialog's list of radio buttons.
         */
        private void initSoundRadioButtons(View v)
        {
            RadioGroup rg = (RadioGroup) v.findViewById(R.id.radio_group);
            for(int i=0; i < this.mSoundUris.size(); i++)
            {
                RadioButton rb = new RadioButton(mContext);
                Uri uri = this.mSoundUris.get(i);
                String title = this.mSoundTitles.get(i);
                rb.setText(title);
                rb.setTag(i);
                rb.setOnCheckedChangeListener(this);
                rg.addView(rb);
            }

            // Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(this,
            //                                                             RingtoneManager.TYPE_NOTIFICATION);
            // Alarm alarm = mCard.getAlarm();
            // this.mEditText.setText(alarm.getName());
        }

        /**
         * @brief Setup the dialog.
         */
        private void initDialog(View v)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(v);
            builder.setTitle("Set Alarm Sound");
            builder.setPositiveButton("OK", this);
            builder.setNegativeButton("Cancel", this);
            this.mDialog = builder.show();
            this.mDialog.setCancelable(true);
            this.mDialog.setCanceledOnTouchOutside(true);
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
                // saveData();
                dialog.dismiss();
                return;
            case DialogInterface.BUTTON_NEGATIVE:
            default:
                dialog.cancel();
                return;
            }
        }

        /**
         * @brief Handles how the ringtones interact when selecting different
         *        radio buttons.
         */
        @Override
        public void onCheckedChanged(CompoundButton b, boolean state)
        {
            if (!state)
            {
                return;
            }
            int i = (int) b.getTag();
            Uri uri = mSoundUris.get(i);
            Ringtone r = mRingtoneManager.getRingtone(mContext, uri);
            // r.setLooping(false);
            mRingtoneManager.stopPreviousRingtone();
            r.play();
        }

    }

}
