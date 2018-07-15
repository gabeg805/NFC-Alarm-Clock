package com.nfcalarmclock;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * @brief The application's main activity.
 */
public class MainActivity
    extends AppCompatActivity
	implements View.OnClickListener
{

    private NacCardRecyclerView mRecyclerView;
	private NacFloatingButton mFloatingButton;

    /**
     * @brief Alarm card adapter.
     */
    private NacCardAdapter mAdapter;

    /**
     * @brief Create the application.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        this.deleteDatabase(NacDatabaseContract.DATABASE_NAME);

		this.mAdapter = new NacCardAdapter(this);
		this.mFloatingButton = new NacFloatingButton(this);
        this.mRecyclerView = new NacCardRecyclerView(this);

        this.mRecyclerView.init();
		this.mFloatingButton.init();
		this.mRecyclerView.setItems(this.mAdapter, this.mFloatingButton);
        this.mAdapter.build();
    }

	/**
	 * @brief Add a new alarm when the floating action button is clicked.
	 */
	@Override
	public void onClick(View v)
	{
        this.mAdapter.add(new Alarm());
	}

    /**
     * @brief The back button was pressed.
     */
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    /**
     * @brief Create the options menu in the action bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.app_bar, menu);
        return true;
    }

    /**
     * @brief A menu item was selected. Determine which action to take depending on
     *        the item selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
        case android.R.id.home:
            Toast.makeText(this, "Yo this is the home",
                           Toast.LENGTH_LONG).show();
            return true;
        case R.id.menu_settings:
            Intent intent = new Intent(getApplicationContext(),
                                       SettingsActivity.class);
            startActivity(intent);
            return true;
        default:
            Toast.makeText(this, "Yo this is the default thing",
                           Toast.LENGTH_LONG).show();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    // @Override
    // protected void onActivityResult(int request, int result, Intent data)
    // {
    //     if (request == NAC_CARD_SOUND_REQUEST)
    //     {
    //         if (result == RESULT_OK)
    //         {
    //             Uri contactUri = data.getData();
    //             mRecyclerView.setSound();
    //         }
    //     }
    // }

}

// AddAlarmFragment addalarm = new AddAlarmFragment();
// FragmentManager manager = getSupportFragmentManager();
// FragmentTransaction transaction = manager.beginTransaction();
// transaction.replace(R.id.fragment_main, addalarm);
// transaction.addToBackStack(null);
// transaction.commit();

// Snackbar.make(view, "Display About",
//               Snackbar.LENGTH_LONG)
//     .setAction("Action", null).show();
