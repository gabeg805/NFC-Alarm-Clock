package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.helper.ItemTouchUIUtil;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.MotionEvent;
import android.view.View;

enum ButtonState
{
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}

public class NacCardTouchHelperCallback
	extends ItemTouchHelper.Callback
{

    private NacCardTouchHelperAdapter mAdapter = null;
	private Canvas mCanvas = null;
	private RecyclerView mRecyclerView = null;
	private ViewHolder mViewHolder = null;
	private float mDx = 0;
	private boolean mActive = false;
	private ButtonState mState = ButtonState.GONE;
	private RectF mRect = null;
	private Paint mPaint = null;
	private static final float mWidth = 100;
	private Bitmap mIconCopy = null;
	private Bitmap mIconDelete = null;

	/**
	 * @param  adapter  The object that overrides the event methods.
	 */
    public NacCardTouchHelperCallback(NacCardTouchHelperAdapter adapter)
	{
        mAdapter = adapter;
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
		NacUtility.printf("Converting to absolute direction %d.", layoutDirection);
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

	/**
	 * @brief Set te movement flags for the card, to allow it to be swiped
	 *        left and right.
	 *
	 * @param  rv  The recycler view.
	 * @param  holder  The view holder.
	 */
    @Override
    public int getMovementFlags(RecyclerView rv, ViewHolder holder)
	{
		NacUtility.printf("Getting movement flags! %b %b" , (rv == null), (holder == null));
        return makeMovementFlags(0,
			ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);
    }

	/**
	 * @brief Allow the card to be swiped.
	 */
    @Override
    public boolean isItemViewSwipeEnabled()
	{
		NacUtility.printf("CHECKING IF ITEM VIEW SWIPE IS ENABLED.");
        return true;
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((NacCard) viewHolder).mCard;
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((NacCard) viewHolder).mCard;
		final View backgroundCopyView = ((NacCard) viewHolder).mBackgroundCopy;
		final View backgroundDeleteView = ((NacCard) viewHolder).mBackgroundDelete;

		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
		{
			if (dX > 0)
			{
				backgroundCopyView.setVisibility(View.VISIBLE);
				backgroundDeleteView.setVisibility(View.GONE);
			}
			else if (dX < 0)
			{
				backgroundCopyView.setVisibility(View.GONE);
				backgroundDeleteView.setVisibility(View.VISIBLE);
			}
		}

        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }


    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((NacCard) viewHolder).mCard;
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

	/**
	 * @brief Disallow movement of cards.
	 *
	 * @param  rv  The recycler view.
	 * @param  holder  The view holder.
	 * @param  target  The target view holder.
	 */
    @Override
    public boolean onMove(RecyclerView rv, ViewHolder holder, ViewHolder target)
	{
		NacUtility.printf("Running onMove.");
        return false;
    }

    @Override
    public void onSelectedChanged(ViewHolder viewHolder, int actionState)
	{
		NacUtility.printf("On Selected Changed!");
        if (viewHolder != null) {
            final View foregroundView = ((NacCard) viewHolder).mCard;

            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		NacUtility.printf("~~~~ Running onSwiped in %d direction.", direction);
        //listener.onItemDelete(viewHolder, direction, viewHolder.getAdapterPosition());
        mAdapter.onItemDelete(viewHolder.getAdapterPosition());
    }

}


			//if (active || (mState == ButtonState.GONE))
			//{
			//	if (dx > 0)
			//	{
			//		drawLeftButton(c, holder, dx);
			//	}
			//	else if (dx < 0)
			//	{
			//		drawRightButton(c, holder, dx);
			//	}
			//}

            //if (mState != ButtonState.GONE)
			//{
            //    if (mState == ButtonState.LEFT_VISIBLE)
			//	{
			//		dx = Math.max(dx, mWidth);
			//		drawLeftButton(c, holder, dx);
			//	}
            //    else if (mState == ButtonState.RIGHT_VISIBLE)
			//	{
			//		dx = Math.min(dx, -mWidth);
			//		drawRightButton(c, holder, dx);
			//	}
            //}
            //else
			//{
			//	rv.setOnTouchListener(this);
            //}

			//Paint p = new Paint();
			//Resources r = ((NacCard)holder).mContext.getResources();
			//View v = ((NacCard)holder).mCard;
			//float top = (float) v.getTop();
			//float bottom = (float) v.getBottom();
			//float left = (float) v.getLeft();
			//float right = (float) v.getRight();
			//float height = bottom - top;
			//float width = height / 3;


			//if (dx > 0)
			//{
			//	p.setColor(Color.parseColor("#388E3C"));

			//	RectF background = new RectF(left, top, dx, bottom);

			//	c.drawRect(background, p);

			//	Bitmap icon = BitmapFactory.decodeResource(r,
			//		R.mipmap.baseline_delete_white_32dp);
			//		//R.drawable.ic_mode_edit_white_24dp);
			//	RectF icon_dest = new RectF(left+width, top+width,
			//		left+2*width, bottom-width);

			//	c.drawBitmap(icon, null, icon_dest, p);
			//}
			//else if (dx < 0)
			//{
			//	p.setColor(Color.parseColor("#D32F2F"));

			//	RectF background = new RectF(right+dx, top, right, bottom);

			//	c.drawRect(background, p);

			//	Bitmap icon = BitmapFactory.decodeResource(r,
			//		R.mipmap.baseline_delete_white_32dp);
			//	RectF icon_dest = new RectF(right-2*width, top+width,
			//		right-width, bottom-width);

			//	c.drawBitmap(icon, null, icon_dest, p);
			//}
			//else
			//{
			//}
