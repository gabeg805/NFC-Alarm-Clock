package com.nfcalarmclock;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.view.View.MeasureSpec;

/**
 * @brief NFC Alarm Clock expand and collapse regions in the alarm card.
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

	/**
	 * @brief The original height of the region when it is collapsed. This
	 * 		  corresponds to the height of the summary region.
	 */
	private int mFromHeight = 0;

	/**
	 * @brief The height once the region is expanded. This corresponds to the
	 * 		  height of the extra region.
	 */
	private int mToHeight = 0;

    /**
     * @brief Expand and collapse regions and buttons.
	 *
	 * @param  r  The root view of the corresponding alarm card.
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
		measureFromHeight();
		measureToHeight();
		collapse();
	}

	/**
	 * @brief Expand the alarm card and animate the view.
	 */
	public void expandAndAnimate()
	{
		expand();
		NacSlideAnimation slide = new NacSlideAnimation(mExtraRegion,
														mFromHeight,
														mToHeight);
		mExtraRegion.startAnimation(slide);
	}

	/**
	 * @brief Collapse the alarm card and animate the view.
	 */
	public void collapseAndAnimate()
	{
		collapse();
		NacSlideAnimation slide = new NacSlideAnimation(mSummaryRegion,
														mToHeight,
														mFromHeight);
		mSummaryRegion.startAnimation(slide);
	}

	/**
	 * @brief Expand the alarm card without animating the view.
	 */
	private void expand()
	{
        this.mSummaryRegion.setVisibility(View.GONE);
        this.mSummaryRegion.setEnabled(false);
        this.mExtraRegion.setVisibility(View.VISIBLE);
        this.mExtraRegion.setEnabled(true);
	}

	/**
	 * @brief Collapse the alarm card without animating the view.
	 */
	private void collapse()
	{
        this.mSummaryRegion.setVisibility(View.VISIBLE);
        this.mSummaryRegion.setEnabled(true);
        this.mExtraRegion.setVisibility(View.GONE);
        this.mExtraRegion.setEnabled(false);
	}

	/**
	 * @brief Measure the height of the collapsed region.
	 */
	private void measureFromHeight()
	{
		collapse();
		mSummaryRegion.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
							   MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		mFromHeight = mSummaryRegion.getMeasuredHeight();
	}

	/**
	 * @brief Measure the height of the expanded region.
	 */
	private void measureToHeight()
	{
		expand();
		mExtraRegion.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
							 MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		mToHeight = mExtraRegion.getMeasuredHeight();
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
