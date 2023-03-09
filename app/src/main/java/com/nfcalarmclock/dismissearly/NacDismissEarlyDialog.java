package com.nfcalarmclock.dismissearly;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.util.dialog.NacDialogFragment;

import java.util.List;

public class NacDismissEarlyDialog
        extends NacDialogFragment
        implements View.OnClickListener
{

    /**
     * Listener for when an audio source is selected.
     */
    @SuppressWarnings("UnnecessaryInterfaceModifier")
    public interface OnDismissEarlyOptionSelectedListener
    {
        public void onDismissEarlyOptionSelected(boolean useDismissEarly, int index);
    }

    /**
     * Tag for the class.
     */
    public static final String TAG = "NacDismissEarlyDialog";

    /**
     * Default dismiss early.
     */
    private boolean mDefaultUseDismissEarly;

    /**
     * Default dismiss early index.
     */
    private int mDefaultDismissEarlyIndex;

    /**
     * Check box to dismiss early or not.
     */
    private MaterialCheckBox mShouldUseDismissEarlyCheckBox;

    /**
     * Summary text for whether dismiss early should be used or not.
     */
    private TextView mShouldUseDismissEarlySummary;

    /**
     * Scrollable picker to choose the dismiss early time.
     */
    private NumberPicker mDismissEarlyTimePicker;

    /**
     * Listener for when the dismiss early option is clicked.
     */
    private NacDismissEarlyDialog.OnDismissEarlyOptionSelectedListener mOnDismissEarlyOptionSelectedListener;

    /**
     * Call the OnDismissEarlyListener object, if it has been set.
     */
    public void callOnDismissEarlyOptionSelectedListener()
    {
        NacDismissEarlyDialog.OnDismissEarlyOptionSelectedListener listener = this.getOnDismissEarlyOptionSelectedListener();

        // Call the listener
        if (listener != null)
        {
            NumberPicker picker = this.getDismissEarlyTimePicker();
            boolean useDismissEarly = this.shouldUseDismissEarly();
            int index = picker.getValue();

            listener.onDismissEarlyOptionSelected(useDismissEarly, index);
        }
    }

    /**
     * Get the default dismiss early index.
     *
     * @return The default dismiss early index.
     */
    public int getDefaultDismissEarlyIndex()
    {
        return this.mDefaultDismissEarlyIndex;
    }

    /**
     * Get the default value for if the dismiss early should be used or not.
     *
     * @return The default dismiss early value.
     */
    public boolean getDefaultUseDismissEarly()
    {
        return this.mDefaultUseDismissEarly;
    }

    /**
     * Get the scrollable picker for the dismiss early time.
     *
     * @return The scrollable picker for the dismiss early time.
     */
    private NumberPicker getDismissEarlyTimePicker()
    {
        return this.mDismissEarlyTimePicker;
    }

    /**
     * Get the OnDismissEarlyListener object.
     *
     * @return The OnDismissEarlyListener object.
     */
    public NacDismissEarlyDialog.OnDismissEarlyOptionSelectedListener getOnDismissEarlyOptionSelectedListener()
    {
        return this.mOnDismissEarlyOptionSelectedListener;
    }

    /**
     * Get the dismiss early check box.
     *
     * @return The dismiss early check box.
     */
    public MaterialCheckBox getShouldUseDismissEarlyCheckBox()
    {
        return this.mShouldUseDismissEarlyCheckBox;
    }

    /**
     * Get the summary text for whether a user should be able to dismiss an
     * alarm early or not.
     *
     * @return The summary text for whether a user should be able to dismiss an
     *         alarm early or not.
     */
    private TextView getShouldUseDismissEarlySummary()
    {
        return this.mShouldUseDismissEarlySummary;
    }

    /**
     * Called when the check box is clicked.
     */
    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if (id == R.id.should_use_dismiss_early)
        {
            this.toggleShouldUseDismissEarly();
            this.setupShouldUseDismissEarlySummary();
            this.setupDismissEarlyTimeEnabled();
        }
    }

    /**
     * Called when the dialog is created.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        this.setupSharedPreferences();

        NacSharedConstants cons = this.getSharedConstants();

        return new AlertDialog.Builder(requireContext())
                .setTitle(cons.getTitleDismissEarly())
                .setPositiveButton(cons.getActionOk(), (dialog, which) ->
                        this.callOnDismissEarlyOptionSelectedListener())
                .setNegativeButton(cons.getActionCancel(), (dialog, which) -> {})
                .setView(R.layout.dlg_alarm_dismiss_early)
                .create();
    }

    /**
     * Called when the fragment is resumed.
     */
    @Override
    public void onResume()
    {
        super.onResume();

        AlertDialog dialog = (AlertDialog) getDialog();
        RelativeLayout container = dialog.findViewById(R.id.should_use_dismiss_early);

        // Set the member variable widgets
        this.mShouldUseDismissEarlyCheckBox =
                dialog.findViewById(R.id.should_use_dismiss_early_checkbox);
        this.mShouldUseDismissEarlySummary =
                dialog.findViewById(R.id.should_use_dismiss_early_summary);
        this.mDismissEarlyTimePicker = dialog.findViewById(R.id.dismiss_early_time_picker);

        // Setup the widgets
        container.setOnClickListener(this);
        this.setupShouldUseDismissEarly();
        this.setupDismissEarlyTimePicker();
        this.setupDismissEarlyTimeEnabled();
        this.setupShouldUseDismissEarlyColor();
    }

    /**
     * Set the default dismiss early time.
     *
     * @param  index  The default dismiss early time.
     */
    public void setDefaultDismissEarlyIndex(int index)
    {
        this.mDefaultDismissEarlyIndex = index;
    }

    /**
     * Set the default dismiss early value.
     *
     * @param  useDismissEarly  The default dismiss early value.
     */
    public void setDefaultUseDismissEarly(boolean useDismissEarly)
    {
        this.mDefaultUseDismissEarly = useDismissEarly;
    }

    /**
     * Set the OnDismissEarlyListener object.
     *
     * @param  listener  The OnDismissEarlyListener object.
     */
    public void setOnDismissEarlyOptionSelectedListener(
            NacDismissEarlyDialog.OnDismissEarlyOptionSelectedListener listener)
    {
        this.mOnDismissEarlyOptionSelectedListener = listener;
    }

    /**
     * Setup the check box and summary text for whether a user should be able
     * to dismiss an alarm early or not.
     */
    private void setupShouldUseDismissEarly()
    {
        boolean useDismissEarly = this.getDefaultUseDismissEarly();

        this.getShouldUseDismissEarlyCheckBox().setChecked(useDismissEarly);
        this.setupShouldUseDismissEarlySummary();
    }

    /**
     * Setup the color of the dismiss early check box.
     */
    private void setupShouldUseDismissEarlyColor()
    {
        NacSharedPreferences shared = this.getSharedPreferences();

        int[] colors = new int[] { shared.getThemeColor(), Color.GRAY };
        int[][] states = new int[][] {
                new int[] {  android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked } };
        ColorStateList colorStateList = new ColorStateList(states, colors);

        this.getShouldUseDismissEarlyCheckBox().setButtonTintList(colorStateList);
    }

    /**
     * Setup the summary text for whether a user should be able to dismiss an
     * alarm early or not.
     */
    private void setupShouldUseDismissEarlySummary()
    {
        boolean useDismissEarly = this.shouldUseDismissEarly();
        int textId = useDismissEarly ? R.string.dismiss_early_true
                : R.string.dismiss_early_false;

        this.getShouldUseDismissEarlySummary().setText(textId);
    }

    /**
     * Get whether an alarm should be able to be dismissed early or not.
     *
     * @return True if an alarm should be able to be dismissed early, and
     *         False otherwise.
     */
    private boolean shouldUseDismissEarly()
    {
        return this.getShouldUseDismissEarlyCheckBox().isChecked();
    }

    /**
     * Toggle whether an alarm should be able to be dismissed early or not.
     */
    private void toggleShouldUseDismissEarly()
    {
        boolean useDismissEarly = this.shouldUseDismissEarly();

        this.getShouldUseDismissEarlyCheckBox().setChecked(!useDismissEarly);
    }

    /**
     * Setup whether the dismiss early time container can be used or not.
     */
    private void setupDismissEarlyTimeEnabled()
    {
        NumberPicker picker = this.getDismissEarlyTimePicker();
        boolean useDismissEarly = this.shouldUseDismissEarly();

        picker.setAlpha(useDismissEarly ? 1.0f : 0.25f);
        picker.setEnabled(useDismissEarly);
    }

    /**
     * Setup scrollable picker for the dismiss early time.
     */
    private void setupDismissEarlyTimePicker()
    {
        NacSharedConstants cons = this.getSharedPreferences().getConstants();
        NumberPicker picker = this.getDismissEarlyTimePicker();
        int index = this.getDefaultDismissEarlyIndex();
        List<String> values = cons.getDismissEarlyTimes();

        picker.setMinValue(0);
        picker.setMaxValue(values.size()-1);
        picker.setDisplayedValues((String[])values.toArray());
        picker.setValue(index);
    }

}
