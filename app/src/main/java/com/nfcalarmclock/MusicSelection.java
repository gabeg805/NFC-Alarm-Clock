package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



/**
 * @brief Music selection item.
 */
public class MusicSelection
	extends LinearLayout
{

	/**
	 * @brief App context.
	 */
	private Context mContext = null;

	/**
	 * @brief Name of the song.
	 */
	private TextView mSongView = null;
	private String mSongName;
    private int mSongTextSize;
    private int mSongTextColor;

	/**
	 * @brief Directory where song is located.
	 */
	private TextView mDirView= null;
	private String mDirName;
    private int mDirTextSize;
    private int mDirTextColor;

	private enum MusicType
	{
		SONG,
		DIRECTORY
	}

	/**
	 */
	public MusicSelection(Context context, @Nullable AttributeSet attrs)
	{
		super(context, attrs);

		this.mContext = context;
        int[] id = R.styleable.MusicSelection;
        int layout = R.layout.musicselection;
        Theme theme = this.mContext.getTheme();
        TypedArray ta = theme.obtainStyledAttributes(attrs, id, 0, 0);

        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(this.mContext).inflate(layout, this, true);
        init(ta);

		//setSong();
		//setDirectory();
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		NacUtility.print("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH finished inflating.");
		setSong();
		setDirectory();
	}

	public void init(TypedArray ta)
	{
		try
		{
			//RelativeLayout.LayoutParams params = new
			//	RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
			//		LayoutParams.WRAP_CONTENT);
			//this.setLayoutParams(params);
			initSong(ta);
			initDirectory(ta);
		}
		finally
		{
			ta.recycle();
		}
	}

    /**
     * @brief Initialize all elements of the text view.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initSong(TypedArray ta)
    {
        Resources r = this.mContext.getResources();
        initView(MusicType.SONG);
        initText(MusicType.SONG, ta);
        initTextColor(MusicType.SONG, ta);
        initTextSize(MusicType.SONG, ta, r);
    }

    /**
     * @brief Initialize all elements of the text view.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initDirectory(TypedArray ta)
    {
        Resources r = this.mContext.getResources();
        initView(MusicType.DIRECTORY);
        initText(MusicType.DIRECTORY, ta);
        initTextColor(MusicType.DIRECTORY, ta);
        initTextSize(MusicType.DIRECTORY, ta, r);
    }
/**
     * @brief Define the text view.
     */
    private void initView(MusicType type)
    {
		TextView tv;

		switch (type)
		{
			case SONG:
				this.mSongView = (TextView) findViewById(R.id.ms_song);
				tv = this.mSongView;
				break;
			case DIRECTORY:
				this.mDirView = (TextView) findViewById(R.id.ms_directory);
				tv = this.mDirView;
				break;
			default:
				return;
		}

        if (tv == null)
        {
            throw new RuntimeException("Unable to create TextView.");
        }
        //<attr name="nacTextColor" />
        //<attr name="nacTextSize" />
        //<attr name="nacText" />
        //<attr name="nacSubTextColor" />
        //<attr name="nacSubTextSize" />
        //<attr name="nacSubText" />
    }

    /**
     * @brief Define the text string.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initText(MusicType type, TypedArray ta)
    {
        int tid;

		switch (type)
		{
			case SONG:
				tid = R.styleable.MusicSelection_nacText;
        		this.mSongName = ta.getString(tid);
				break;
			case DIRECTORY:
				tid = R.styleable.MusicSelection_nacSubText;
        		this.mDirName = ta.getString(tid);
				break;
			default:
				return;
		}
    }

    /**
     * @brief Define the color of the text.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initTextColor(MusicType type, TypedArray ta)
    {
        int cid;
        int color;

		switch (type)
		{
			case SONG:
				cid = R.styleable.MusicSelection_nacTextColor;
				color = NacUtility.getThemeAttrColor(this.mContext,
							R.attr.colorCardDrawable);
        		this.mSongTextColor = ta.getColor(cid, color);
				break;
			case DIRECTORY:
				cid = R.styleable.MusicSelection_nacSubTextColor;
				color = NacUtility.getThemeAttrColor(this.mContext,
							R.attr.colorCardDrawable);
        		this.mDirTextColor = ta.getColor(cid, color);
				break;
			default:
				return;
		}
    }

    /**
     * @brief Define the size of the text.
     * 
     * @param ta Array of values that were retrieved from the context's theme.
     */
    private void initTextSize(MusicType type, TypedArray ta,
		Resources r)
    {
        int tsid;
        int size;

		switch (type)
		{
			case SONG:
				tsid = R.styleable.MusicSelection_nacTextSize;
				size = (int) r.getDimension(R.dimen.tsz_card);
				this.mSongTextSize = ta.getDimensionPixelSize(tsid, size);
				break;
			case DIRECTORY:
				tsid = R.styleable.MusicSelection_nacSubTextSize;
				size = (int) r.getDimension(R.dimen.tsz_card);
				this.mDirTextSize = ta.getDimensionPixelSize(tsid, size);
				break;
			default:
				return;
		}
    }

	public void setSong()
	{
		TextView tv = this.mSongView;

		if (tv == null)
		{
			throw new RuntimeException("Unable to find textview ID for song.");
		}

		NacUtility.printf("Text : %s | Color : %d | Size : %d", mSongName, mSongTextColor, mSongTextSize);
		tv.setText(this.mSongName);
		tv.setTextColor(this.mSongTextColor);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.mSongTextSize);
	}

	public void setDirectory()
	{
		TextView tv = this.mDirView;
		TextView other = this.mSongView;

		if (tv == null)
		{
			throw new RuntimeException("Unable to find textview ID for directory.");
		}

		tv.setText(this.mDirName);
		tv.setTextColor(this.mDirTextColor);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.mDirTextSize);
	}

	public void setSongName(String name)
	{
		this.mSongName = name;
		this.mSongView.setText(this.mSongName);
	}

	public void setDirName(String dir)
	{
		this.mDirName = dir;
		this.mDirView.setText(this.mDirName);
	}

}
