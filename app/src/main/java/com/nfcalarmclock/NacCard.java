package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.LinearLayout;

import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.LinearLayoutManager;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.transition.Transition;
import android.os.Handler;

import android.util.DisplayMetrics;

import android.view.View.MeasureSpec;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.ViewGroup.LayoutParams;

/**
 * @brief Holder of all important views.
 */
public class NacCard
    extends RecyclerView.ViewHolder
    implements View.OnCreateContextMenuListener,View.OnClickListener
{

    public AppCompatActivity mActivity;
    public Context mContext;
    public CardView mCard;
	public NacCardRegion mRegion;
    public NacCardSwitch mSwitch;
    public NacCardTime mTime;
    public NacCardRepeat mRepeat;
    public NacCardSound mSound;
    public NacCardVibrate mVibrate;
    public NacCardName mName;
    public NacCardDelete mDelete;

	private int mExpandedHeight = 0;
	private int mCollapsedHeight = 0;

    /**
     * @brief The alarm data used in populating this alarm card.
     */
    private Alarm mAlarm;

    /**
     * @brief Define all views in the holder.
     */
    public NacCard(Context c, View r)
    {
        super(r);
        this.mActivity = (AppCompatActivity) c;
        this.mContext = c;
        this.mCard = (CardView) r.findViewById(R.id.view_card_alarm);

		this.mRegion = new NacCardRegion(r);
        this.mTime = new NacCardTime(c, r);
        this.mSwitch = new NacCardSwitch(r);
        this.mRepeat = new NacCardRepeat(r);
        this.mSound = new NacCardSound(c, r);
        this.mVibrate = new NacCardVibrate(r);
        this.mName = new NacCardName(c, r);
        this.mDelete = new NacCardDelete(r);

	}

	public void setHeight()
	{
		//mRegion.collapse();

		//mCard.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
		//			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		//mCollapsedHeight = mCard.getMeasuredHeight();
		//NacUtility.printf("Setting Collapsed Height : %d", mCollapsedHeight);

		//mRegion.expand();
		////mCard.requestLayout();

		//mCard.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
		//			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		//mExpandedHeight = mCard.getMeasuredHeight();
		//NacUtility.printf("Setting Expanded Height : %d", mExpandedHeight);

		//mRegion.collapse();
	}

    /**
     * @brief Initialize the alarm card.
     */
    public void init(Alarm alarm, int pos)
    {
        this.mAlarm = alarm;

		this.mRegion.init();
        this.mTime.init(alarm);
        this.mSwitch.init(alarm);
        this.mRepeat.init(alarm);
        this.mSound.init(alarm);
        this.mVibrate.init(alarm);
        this.mName.init(alarm);
		this.mDelete.init(pos);

        this.mActivity.registerForContextMenu(this.mCard);
        this.mCard.setOnCreateContextMenuListener(this);
		this.mRegion.setExpandListener(this);
		this.mRegion.setCollapseListener(this);
    }

	/**
	 * @brief Focus the alarm card.
	 */
	public void focus()
	{
		int duration = 2200;
		int bg = NacUtility.getThemeAttrColor(this.mContext,
											R.attr.colorCard);
		int highlight= NacUtility.getThemeAttrColor(this.mContext,
											R.attr.colorCardExpanded);
		ColorDrawable[] color = {new ColorDrawable(highlight),
								new ColorDrawable(bg)};
		TransitionDrawable transition = new TransitionDrawable(color);

		this.mCard.setBackgroundDrawable(transition);
		transition.startTransition(duration);

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				NacUtility.printf("Resetting background.");
				//TransitionDrawable td = (TransitionDrawable) mCard.getBackground();
				//td.reverseTransition(2000);
				backgroundReset();
			}
		}, duration);
	}

    /**
     * @brief Expand the alarm card.
     */
    public void expand()
    {
		scrollToTop();
		mRegion.expand();
		backgroundExpanded();

        //mCard.getLayoutParams().height = mCollapsedHeight;
        //Animation a = new Animation()
        //{
        //    @Override
        //    protected void applyTransformation(float interpolatedTime, Transformation t) {
		//		NacUtility.printf("Interpolated Time : %f", interpolatedTime);
		//		int height = 0;

		//		if (interpolatedTime == 1)
		//		{
		//			height = LayoutParams.WRAP_CONTENT;
		//		}
		//		else
		//		{
		//			height = (int)(mExpandedHeight * interpolatedTime);
		//		}

        //        mCard.getLayoutParams().height = height;
        //        mCard.requestLayout();
		//		NacUtility.printf("Transformation : %d", mCard.getLayoutParams().height);
        //    }

        //    @Override
        //    public boolean willChangeBounds() {
        //        return true;
        //    }
        //};

		//int duration = (int)(mExpandedHeight / mCard.getContext().getResources().getDisplayMetrics().density);
		//NacUtility.printf("Duration : %d", duration);
        //a.setDuration(duration);
        //mCard.startAnimation(a);

    }

    /**
     * @brief Collapse the alarm card.
     */
    public void collapse()
    {
		mRegion.collapse();
		backgroundReset();
		//mCard.animate().translationY(mCollapsedHeight);

        //final int initialHeight = mCard.getMeasuredHeight();
		//NacUtility.printf("Initial Height : %d", initialHeight);

        //Animation a = new Animation()
        //{
        //    @Override
        //    protected void applyTransformation(float interpolatedTime, Transformation t) {
        //        if (interpolatedTime == 1)
		//		{
        //            //mCard.setVisibility(View.GONE);
        //        }
		//		else
		//		{
		//			int height = initialHeight - (int)(initialHeight * interpolatedTime);
		//			if (height < mCollapsedHeight)
		//			{
		//				height = mCollapsedHeight;
		//			}
        //            mCard.getLayoutParams().height = height;
        //            mCard.requestLayout();
		//			NacUtility.printf("Collapse Transformation : %d", mCard.getLayoutParams().height);
        //        }
        //    }

        //    @Override
        //    public boolean willChangeBounds() {
        //        return true;
        //    }
        //};

		//int duration = (int)(initialHeight / mCard.getContext().getResources().getDisplayMetrics().density);
		//NacUtility.printf("Collapse duration : %d", duration);
        //a.setDuration(duration);
        //mCard.startAnimation(a);
    }

	/**
	 * @brief Scroll card view to the top of the screen.
	 */
	public void scrollToTop()
	{
		int pos = getAdapterPosition();
		RecyclerView.SmoothScroller scroller = getSmoothScroller(pos);
        RecyclerView rv = (RecyclerView) mActivity.findViewById(R.id.content_alarm_list);
        rv.getLayoutManager().startSmoothScroll(scroller);
		//rv.scrollToPosition(pos);
	}

	/**
	 * @brief Reset background color of the alarm card.
	 */
	public void backgroundReset()
	{
		int bg = NacUtility.getThemeAttrColor(this.mContext, R.attr.colorCard);
		this.mCard.setBackground(null);
		this.mCard.setBackgroundColor(bg);
	}

	/**
	 * @brief Set background color of the alarm card to the expanded color.
	 */
	public void backgroundExpanded()
	{
		int bg = NacUtility.getThemeAttrColor(this.mContext,
											R.attr.colorCardExpanded);
		this.mCard.setBackground(null);
		this.mCard.setBackgroundColor(bg);
	}

	/**
	 * @brief Set background color of the alarm card to the highlight color.
	 */
	public void backgroundHighlight()
	{
		int bg = NacUtility.getThemeAttrColor(this.mContext,
											R.attr.colorCardHighlight);
		this.mCard.setBackground(null);
		this.mCard.setBackgroundColor(bg);
	}

	/**
	 * @brief Create a smooth scroller that scrolls to the top of the screen.
	 */
    public RecyclerView.SmoothScroller getSmoothScroller(int pos)
    {
		RecyclerView.SmoothScroller scroller =
			new LinearSmoothScroller(mContext)
		{
			private final float mpi = 100f;
			@Override
			protected int getVerticalSnapPreference()
			{
				return LinearSmoothScroller.SNAP_TO_START;
			}

			@Override
			protected float calculateSpeedPerPixel(DisplayMetrics dm)
			{
				return mpi / dm.densityDpi;
			}
		};
		scroller.setTargetPosition(pos);
		return scroller;
    }

	/**
	 * @brief Set listener to delete the card.
	 */
	public void setDeleteListener(View.OnClickListener listener)
	{
		this.mDelete.setListener(listener);
	}

    /**
     * @brief Display the context menu for the selected alarm card.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo)
    {
        MenuInflater inflater = mActivity.getMenuInflater();
        inflater.inflate(R.menu.alarm_card, menu);
    }

	/**
	 * @brief Expand or collapse the card.
	 *
	 * @details When the alarm card is not at the top of the screen, clicking
	 * 			the expand button will scroll the screen so that the alarm card
	 * 			is at the top.
	 */
	@Override
	public void onClick(View v)
	{
		int id = v.getId();

		switch (id)
		{
			case R.id.nacExpand:
				expand();
				break;
			case R.id.nacCollapse:
				collapse();
				break;
			default:
				break;
		}
	}

}
