package com.nfcalarmclock;

import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;

public class SlideAnimation
	extends Animation
{

    View mView;
    int mFromHeight;
    int mToHeight;

    public SlideAnimation(View v, int from, int to)
	{
        this.mView = v;
        this.mFromHeight = from;
        this.mToHeight = to;
    }

    @Override
    protected void applyTransformation(float time, Transformation trans)
	{
        int newHeight;

        if (mView.getHeight() != mToHeight)
		{
            newHeight = (int) (mFromHeight + ((mToHeight - mFromHeight) * time));
            mView.getLayoutParams().height = newHeight;
            mView.requestLayout();
        }
    }

    @Override
    public void initialize(int width, int height,
						   int parentWidth, int parentHeight)
	{
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds()
	{
        return true;
    }

}
