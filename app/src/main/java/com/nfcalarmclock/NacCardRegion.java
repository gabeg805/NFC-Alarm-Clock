package com.nfcalarmclock;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import android.util.DisplayMetrics;

import android.view.View.MeasureSpec;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.ViewGroup.LayoutParams;

/**
 * @brief Expand and collapse regions in the alarm card.
 */
public class NacCardRegion
{

    /**
     * @brief Summary region.
     */
    private RelativeLayout mSummaryRegion;

    /**
     * @brief Extra region.
     */
    private RelativeLayout mExtraRegion;

    /**
     * @brief Expand button.
     */
    private ImageView mExpandButton;

    /**
     * @brief Collapse button.
     */
    private ImageView mCollapseButton;

	private int mExpandedHeight = 0;
	private int mCollapsedHeight = 0;
	private boolean mStuff = false;

    /**
     * @brief Constructor.
     */
    public NacCardRegion(View r)
    {
		this.mSummaryRegion = (RelativeLayout) r.findViewById(R.id.alarmMinorSummary);
		this.mExtraRegion = (RelativeLayout) r.findViewById(R.id.alarmMinorExpand);
        this.mExpandButton = (ImageView) r.findViewById(R.id.nacExpand);
        this.mCollapseButton = (ImageView) r.findViewById(R.id.nacCollapse);
    }

	/**
	 * @brief Initialize the summary and expandable regions.
	 */
	public void init()
	{
        this.mExtraRegion.setVisibility(View.GONE);
        this.mExtraRegion.setEnabled(false);

		collapse();

		mSummaryRegion.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		mCollapsedHeight = mSummaryRegion.getMeasuredHeight();
		NacUtility.printf("Setting Collapsed Height : %d", mCollapsedHeight);

		expand();
		//mCard.requestLayout();

		mExtraRegion.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		mExpandedHeight = mExtraRegion.getMeasuredHeight();
		NacUtility.printf("Setting Expanded Height : %d", mExpandedHeight);

		collapse();
		mStuff = true;
	}

	/**
	 * @brief Expand the card region.
	 */
	public void expand()
	{
        this.mSummaryRegion.setVisibility(View.GONE);
        this.mSummaryRegion.setEnabled(false);
        this.mExtraRegion.setVisibility(View.VISIBLE);
        this.mExtraRegion.setEnabled(true);

		if (mStuff)
		{
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
				NacUtility.printf("Interpolated Time : %f", interpolatedTime);
				int height = 0;

				if (interpolatedTime == 1)
				{
					height = LayoutParams.WRAP_CONTENT;
				}
				else
				{
					height = (int)(mExpandedHeight * interpolatedTime);
				}

                mExtraRegion.getLayoutParams().height = height;
                mExtraRegion.requestLayout();
				NacUtility.printf("Expand Transformation 1: %d", mExtraRegion.getLayoutParams().height);
				NacUtility.printf("Expand Transformation 2: %d", mSummaryRegion.getLayoutParams().height);
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

		int duration = (int)(mExpandedHeight / mExtraRegion.getContext().getResources().getDisplayMetrics().density);
		NacUtility.printf("Duration : %d", duration);
        a.setDuration(duration);
        mExtraRegion.startAnimation(a);
		}
	}

	/**
	 * @brief Collapse the card region.
	 */
	public void collapse()
	{
        this.mSummaryRegion.setVisibility(View.VISIBLE);
        this.mSummaryRegion.setEnabled(true);
        this.mExtraRegion.setVisibility(View.GONE);
        this.mExtraRegion.setEnabled(false);

		if (mStuff)
		{
        final int initialHeight = mExtraRegion.getMeasuredHeight();
		NacUtility.printf("Initial Height : %d", initialHeight);

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1)
				{
                    //mCard.setVisibility(View.GONE);
                }
				else
				{
					int height = initialHeight - (int)(initialHeight * interpolatedTime);
					if (height < mCollapsedHeight)
					{
						height = mCollapsedHeight;
					}
                    mSummaryRegion.getLayoutParams().height = height;
                    mSummaryRegion.requestLayout();
					NacUtility.printf("Collapse Transformation 1: %d", mExtraRegion.getLayoutParams().height);
					NacUtility.printf("Collapse Transformation 2: %d", mSummaryRegion.getLayoutParams().height);
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

		int duration = (int)(initialHeight / mSummaryRegion.getContext().getResources().getDisplayMetrics().density);
		NacUtility.printf("Collapse duration : %d", duration);
        a.setDuration(duration);
        mSummaryRegion.startAnimation(a);
		}
	}

	/**
	 * @brief Return the expand region.
	 */
	public RelativeLayout getExpandRegion()
	{
		return this.mExtraRegion;
	}

	/**
	 * @brief Return the collapse region.
	 */
	public RelativeLayout getCollapseRegion()
	{
		return this.mSummaryRegion;
	}

	/**
	 * @brief Set a listener to expand the card.
	 */
	public void setExpandListener(View.OnClickListener listener)
	{
        this.mExpandButton.setOnClickListener(listener);
	}

	/**
	 * @brief Set a listener to collapse the card.
	 */
	public void setCollapseListener(View.OnClickListener listener)
	{
        this.mCollapseButton.setOnClickListener(listener);
	}

}
