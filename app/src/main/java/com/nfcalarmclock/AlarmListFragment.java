package com.nfcalarmclock;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.support.v7.widget.RecyclerView;
// import android.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

public class AlarmListFragment extends AppCompatActivity
{
    private TextView mTextViewEmpty;
    private ProgressBar mProgressBarLoading;
    private ImageView mImageViewEmpty;
    private RecyclerView mRecyclerView;
    private ListAdapter mListadapter;

    // @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mTextViewEmpty = (TextView)view.findViewById(R.id.textViewEmpty);
        mImageViewEmpty = (ImageView)view.findViewById(R.id.imageViewEmpty);
        mProgressBarLoading = (ProgressBar)view.findViewById(R.id.progressBarLoading);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getSupportActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        ArrayList data = new ArrayList<DataNote>();
        for (int i = 0; i < DataNoteImformation.id.length; i++)
        {
            data.add(
                new DataNote
                (
                    DataNoteImformation.id[i],
                    DataNoteImformation.textArray[i],
                    DataNoteImformation.dateArray[i]
                    ));
        }

        mListadapter = new ListAdapter(data);
        mRecyclerView.setAdapter(mListadapter);

        return view;
    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>
    {
        private ArrayList<DataNote> dataList;

        public ListAdapter(ArrayList<DataNote> data)
        {
            this.dataList = data;
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView textViewText;
            TextView textViewComment;
            TextView textViewDate;

            public ViewHolder(View itemView)
            {
                super(itemView);
                this.textViewText = (TextView) itemView.findViewById(R.id.text);
                this.textViewComment = (TextView) itemView.findViewById(R.id.comment);
                this.textViewDate = (TextView) itemView.findViewById(R.id.date);
            }
        }

        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ListAdapter.ViewHolder holder, final int position)
        {
            holder.textViewText.setText(dataList.get(position).getText());
            holder.textViewComment.setText(dataList.get(position).getComment());
            holder.textViewDate.setText(dataList.get(position).getDate());

            holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Toast.makeText(getActivity(), "Item " + position + " is clicked.", Toast.LENGTH_SHORT).show();
                    }
                });
        }

        @Override
        public int getItemCount()
        {
            return dataList.size();
        }
    }


}

public class DataNote
{
    String text;
    String comment;
    String date;

    public DataNote(String text, String comment, String date)
    {
        this.text = text;
        this.comment = comment;
        this.date = date;
    }

    public String getText()
    {
        return text;
    }

    public String getComment()
    {
        return comment;
    }

    public String getDate()
    {
        return date;
    }
}

public class DataNoteImformation
{
    public static String[] textArray = {"Lightshot"};
    public static String[] dateArray = {"2017-04-25"};
    public static String[] id = {"1"};
}

//     /* When you're ready, set this back to false. You need to add a timing
//      * thing, where if you don't hit back before the toast finishes, then it
//      * resets back to false. */
//     private static boolean quitApp = true;

//     @Override
//     protected void onCreate(Bundle savedInstanceState)
//     {
//         super.onCreate(savedInstanceState);
//         setContentView(R.layout.content_alarm_list);

//         // /* Toolbar */
//         // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//         // setSupportActionBar(toolbar);

//         // /* Add a new alarm */
//         // FloatingActionButton newalarm = (FloatingActionButton) findViewById(R.id.button_new_alarm);
//         // newalarm.setOnClickListener(new View.OnClickListener()
//         //     {
//         //         @Override
//         //         public void onClick(View view)
//         //         {
//         //             Snackbar.make(view, "Replace with your own action",
//         //                           Snackbar.LENGTH_LONG)
//         //                 .setAction("Action", null).show();
//         //         }
//         //     });

//         // /* Or is this the navigation drawer */
//         // DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//         // ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//         //     this, drawer, toolbar, R.string.navigation_drawer_open,
//         //     R.string.navigation_drawer_close);
//         // drawer.addDrawerListener(toggle);
//         // toggle.syncState();

//         // /* Navigation drawer, I think */
//         // NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//         // navigationView.setNavigationItemSelectedListener(this);
//     }

//     @Override
//     public void onBackPressed()
//     {
//         if (!quitApp)
//         {
//             Toast.makeText(getApplicationContext(), R.string.quit_message,
//                            Toast.LENGTH_SHORT).show();
//             quitApp = true;
//         }
//         else
//         {
//             quitApp = false;
//             moveTaskToBack(true);
//         }
//     }
// }
