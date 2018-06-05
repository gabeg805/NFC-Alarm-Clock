package com.nfcalarmclock;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

// Enter listener
import android.widget.TextView.OnEditorActionListener;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.text.InputType;

/* For delete thing */
import android.widget.Toast;

/* Sound */
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

/* Name */
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

/**
 * @brief Alarm card adapter.
 */
public class AlarmCardAdapter
    extends RecyclerView.Adapter<AlarmCard>
{

    /**
     * @brief The Activity of the parent.
     */
    private AppCompatActivity mActivity;

    /**
     * @brief The Context of the parent.
     */
    private Context mContext;

    /**
     * @brief List of alarms.
     */
    private List<Alarm> mAlarmList;

    /**
     * @brief Current adapter position.
     */
    private int mPosition;

    /**
     * @brief The RecyclerView containing the adapter.
     */
    private RecyclerView mRecyclerView;

    /**
     * @brief The EditText in the dialog when setting the name of an alarm.
     */
    private EditText mEditText;

    /**
     * @brief Alarm adapter.
     */
    public AlarmCardAdapter(Context context)
    {
        this.mActivity = (AppCompatActivity) context;
        this.mContext = context;
        this.mAlarmList = new ArrayList<>();
        this.mRecyclerView = this.mActivity.findViewById(R.id.content_alarm_list);
    }

    /**
     * @brief Create the view holder.
     */
    @Override
    public AlarmCard onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        View item = LayoutInflater.from(context).inflate(
            R.layout.view_card_alarm, parent, false);
        AlarmCard holder = new AlarmCard(mContext, item);
        holder.init();
        holder.setShowMenuListener();
        holder.setExpandListener();
        holder.setCollapseListener();
        holder.setDeleteListener(this.DeleteListener);
        holder.setTimePickerListener(this.TimePickerListener);
        holder.setSwitchListener(this.SwitchListener);
        holder.setRepeatListener(this.RepeatListener);
        holder.setSoundSetListener(this.SoundSetListener);
        holder.setVibrateListener(this.VibrateListener);
        holder.setNameSetListener(this.NameSetListener);
        Log.e("NFCAlarmClock", "onCreateViewHolder was called.");
        return holder;
    }

    // AFTER SETTING TIME, THIS GETS CALLED AND RESETS THE VIEWS. FIGURE OUT HOW TO BIND CORRECTLY

    /**
     * @brief Bind the view holder.
     */
    @Override
    public void onBindViewHolder(final AlarmCard holder, int position)
    {
        Log.e("NFCAlarmClock", "onBindViewHolder was called at position: "+String.valueOf(position));
        Alarm alarm = mAlarmList.get(position);
        this.mPosition = getPosition(holder);
        holder.setAlarm(alarm);
        holder.collapse();
    }

    /**
     * @brief Return the total number of alarms.
     * 
     * @return The total number of alarms.
     */
    @Override
    public int getItemCount()
    {
        return mAlarmList.size();
    }

    /**
     * @brief Return the Alarm for the given View.
     * 
     * @return The Alarm associated with the given View.
     */
    public Alarm getAlarm(View v)
    {
        return getAlarm(getPosition(v));
    }

    /**
     * @brief Return the Alarm at the given position.
     * 
     * @return The Alarm associated with the given position.
     */
    public Alarm getAlarm(int position)
    {
        return mAlarmList.get(position);
    }

    /**
     * @brief Return the position for the given View.
     * 
     * @return The position for the given View.
     */
    public int getPosition(View v)
    {
        ViewHolder holder = (ViewHolder) v.getTag();
        return getPosition(holder);
    }

    /**
     * @brief Return the position for the given ViewHolder.
     * 
     * @return The position for the given ViewHolder.
     */
    public int getPosition(ViewHolder h)
    {
        return h.getAdapterPosition();
    }

    /**
     * @brief Return the ViewHolder at the given position.
     * 
     * @return The ViewHolder at the given position.
     */
    public ViewHolder getViewHolder(int position)
    {
        return mRecyclerView.findViewHolderForAdapterPosition(position);
    }

    // // Called by RecyclerView when it starts observing this Adapter.
    // @Override
    // public void onAttachedToRecyclerView(RecyclerView recyclerView)
    // {
    //     Log.e("NFCAlarmClock", "onAttachedToRecyclerView was called.");
    //     super.onAttachedToRecyclerView(recyclerView);
    // }

    // // Called by RecyclerView when it stops observing this Adapter.
    // @Override
    // public void onDetachedFromRecyclerView(RecyclerView recyclerView)
    // {
    //     Log.e("NFCAlarmClock", "onDetachedFromRecyclerView was called.");
    //     super.onDetachedFromRecyclerView(recyclerView);
    // }

    /**
     * @brief Add an alarm.
     */
    public void add(Alarm alarm)
    {
        this.mAlarmList.add(alarm);
        this.notifyItemInserted(this.getItemCount()+1);
    }

    /**
     * @brief Delete the alarm card.
     */
    View.OnClickListener DeleteListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(mContext, "Deleted.",
                               Toast.LENGTH_SHORT).show();
                int position = getPosition(view);
                mAlarmList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(0, getItemCount());
            }
        };

    /**
     * @brief Display the time picker dialog.
     */
    View.OnClickListener TimePickerListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(mContext, "Time Set!",
                               Toast.LENGTH_SHORT).show();
                AlarmCard holder = (AlarmCard) view.getTag();
                Alarm alarm = getAlarm(view);
                TextView time = holder.hourminute;
                TextView meridian = holder.meridian;
                AlarmTimePicker dialog = new AlarmTimePicker();
                FragmentManager manager = mActivity.getSupportFragmentManager();
                dialog.init(alarm, time, meridian);
                dialog.show(manager, "AlarmTimePicker");
            }
        };

    /**
     * @brief Save the enabled state of the alarm.
     */
    CompoundButton.OnCheckedChangeListener SwitchListener =
        new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton view,
                                         boolean isChecked)
            {
                Alarm alarm = getAlarm(view);
                alarm.setEnabled(isChecked);
            }
        };

    /**
     * @brief Save the repeat state of the alarm.
     */
    CompoundButton.OnCheckedChangeListener RepeatListener =
        new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton view,
                                         boolean isChecked)
            {
                Alarm alarm = getAlarm(view);
                alarm.setRepeat(isChecked);
            }
        };

    /**
     * @brief Display the dialog to set the sound that will be played when the
     *        alarm is activated.
     */
    View.OnClickListener SoundSetListener =
        new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(mContext, "Sound.",
                               Toast.LENGTH_SHORT).show();
                
                mPosition = getPosition(view);
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                                RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                                "Select Alarm Sound");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                                false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
                                true);
                mActivity.startActivityForResult(intent, 5);
            }
        };

    /**
     * @brief Save the vibrate state of the alarm.
     */
    CompoundButton.OnCheckedChangeListener VibrateListener =
        new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton view,
                                         boolean isChecked)
            {
                Alarm alarm = getAlarm(view);
                alarm.setVibrate(isChecked);
            }
        };

    /**
     * @brief Display the dialog to set the alarm name.
     */
    View.OnClickListener NameSetListener =
        new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(mContext, "Named.",
                               Toast.LENGTH_SHORT).show();

                mPosition = getPosition(view);
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				LayoutInflater inflater = LayoutInflater.from(mContext);
				View prompt = inflater.inflate(R.layout.dlg_alarm_name, null);
                Alarm alarm = getAlarm(mPosition);
				mEditText = (EditText) prompt.findViewById(R.id.editTextDialogUserInput);
                mEditText.setText(alarm.getName());
                mEditText.selectAll();
                mEditText.setOnEditorActionListener(NameSetDialogEnterListener);
                mEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
                // mEditText.setImeActionLabel(mActivity.getResources().getString(R.string.done), EditorInfo.IME_ACTION_DONE);
                mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
				builder.setView(prompt);
                builder.setTitle("Set Alarm Name");
                // .setCancelable(false)
				builder.setPositiveButton("OK", NameSetDialogOkListener);
                builder.setNegativeButton("Cancel", NameSetDialogCancelListener);
                builder.show();
            }
        };

    TextView.OnEditorActionListener NameSetDialogEnterListener =
        new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event)
            {
                // InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                // imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                if (event == null)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // Capture soft enters in a singleLine EditText that is the last EditText
                        // This one is useful for the new list case, when there are no existing ListItems
                        Log.e("NFCAlarmClock", "DONE.");
                        mEditText.clearFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    } else if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        // Capture soft enters in other singleLine EditTexts
                        Log.e("NFCAlarmClock", "NEXT.");
                    } else if (actionId == EditorInfo.IME_ACTION_GO) {
                        Log.e("NFCAlarmClock", "GO.");
                    } else {
                        // Let the system handle all other null KeyEvents
                        Log.e("NFCAlarmClock", "NULL.");
                        return false;
                    }
                } else if (actionId == EditorInfo.IME_NULL) {
                    // Capture most soft enters in multi-line EditTexts and all hard enters;
                    // They supply a zero actionId and a valid keyEvent rather than
                    // a non-zero actionId and a null event like the previous cases.
                        Log.e("NFCAlarmClock", "IME NULL.");
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        // We capture the event when the key is first pressed.
                        Log.e("NFCAlarmClock", "ACTION DOWN.");
                    } else {
                        // We consume the event when the key is released.
                        Log.e("NFCAlarmClock", "KEY RELEASED.");
                        return true;
                    }
                } else {
                    // We let the system handle it when the listener is triggered by something that
                    // wasn't an enter.
                        Log.e("NFCAlarmClock", "SOMETHING ELSE.");
                    return false;
                }

                return true;

                // Toast.makeText(mContext, "EnterListener.",
                //                Toast.LENGTH_SHORT).show();

                // AlarmCard holder = (AlarmCard) getViewHolder(mPosition);
                // Alarm alarm = getAlarm(mPosition);
                // String text = v.getText().toString();
                // alarm.setName(text);
                // holder.name.setText(text);
                // return true;
            }
        };

    DialogInterface.OnClickListener NameSetDialogOkListener =
        new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                AlarmCard holder = (AlarmCard) getViewHolder(mPosition);
                Alarm alarm = getAlarm(mPosition);
                String text = mEditText.getText().toString();
                alarm.setName(text);
                holder.name.setText(text);
            }
        };

    DialogInterface.OnClickListener NameSetDialogCancelListener =
        new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        };

}
