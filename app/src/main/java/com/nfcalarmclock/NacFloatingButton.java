package com.nfcalarmclock;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * @brief Recycler view for all alarm cards.
 */
public class NacFloatingButton
    implements View.OnClickListener
{

    /**
     * @brief Activity.
     */
    private AppCompatActivity mActivity;

    /**
     * @return Floating action button to add new alarms.
     */
    private FloatingActionButton mFloatingButton;

    /**
     * @brief Container for displaying the list of alarm cards.
     */
    private NacCardRecyclerView mRecyclerView;

    /**
     * @brief Constructor.
     */
    public NacFloatingButton(AppCompatActivity activity)
    {
        this.mActivity = activity;
        this.mFloatingButton = (FloatingActionButton) activity.findViewById(R.id.fab_add_alarm);
        this.mFloatingButton.setOnClickListener(this);
    }

    /**
     * @brief Initialize the floating button.
     */
    public void init(NacCardRecyclerView rv)
    {
        this.mRecyclerView = rv;
    }

    /**
     * @brief Show the floating button.
     */
    public void show()
    {
        this.mFloatingButton.show();
    }

    /**
     * @brief Hide the floating button.
     */
    public void hide()
    {
        this.mFloatingButton.hide();
    }

    /**
     * @brief Check if floating button is shown.
     */
    public boolean isShown()
    {
        return this.mFloatingButton.isShown();
    }

    /**
     * @brief Create a new alarm card.
     */
    @Override
    public void onClick(View view)
    {
        Toast.makeText(mActivity, "Adding a new alarm!", 
                       Toast.LENGTH_LONG).show();
        mRecyclerView.addAlarm();
    }

}
