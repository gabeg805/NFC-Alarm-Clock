package com.nfcalarmclock.card;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Alarm card adapter.
 */
@SuppressWarnings("UnnecessaryInterfaceModifier")
public class NacCardAdapter
	extends ListAdapter<NacAlarm, NacCardHolder>
{

	/**
	 * Listener for when an alarm card is bound.
	 */
	public interface OnViewHolderBoundListener
	{
		public void onViewHolderBound(NacCardHolder holder, int index);
	}

	/**
	 * Listener for when an alarm card is created.
	 */
	public interface OnViewHolderCreatedListener
	{
		public void onViewHolderCreated(NacCardHolder holder);
	}

	/**
	 * Listener for when an alarm card is bound.
	 */
	private OnViewHolderBoundListener mOnViewHolderBoundListener;

	/**
	 * Listener for when an alarm card is created.
	 */
	private OnViewHolderCreatedListener mOnViewHolderCreatedListener;

	/**
	 * Indices of the cards that are expanded.
	 */
	private List<Integer> mIndicesOfExpandedCards;

	/**
	 */
	public static final DiffUtil.ItemCallback<NacAlarm> DIFF_CALLBACK =
		new DiffUtil.ItemCallback<NacAlarm>() {

			/**
			 */
			@Override
			public boolean areItemsTheSame(@NonNull NacAlarm oldAlarm,
				@NonNull NacAlarm newAlarm)
			{
				//NacUtility.printf("areItemsTheSame? %d | %d", oldAlarm.getId(), newAlarm.getId());
				return oldAlarm.equalsId(newAlarm);
			}

			/**
			 */
			@Override
			public boolean areContentsTheSame(@NonNull NacAlarm oldAlarm,
				@NonNull NacAlarm newAlarm)
			{
				// NOTE: if you use equals, your object must properly override Object#equals()
				// Incorrectly returning false here will result in too many animations.
				//NacUtility.printf("areContentsTheSame? %d | %d", oldAlarm.getId(), newAlarm.getId());
				return oldAlarm.equals(newAlarm);
			}
		};

	/**
	 */
	public NacCardAdapter()
	{
		super(DIFF_CALLBACK);

		setHasStableIds(true);
	}

	///**
	// * @return True if the maximum number of alarms has been created, and False
	// *     otherwise.
	// */
	//public boolean atMaxAlarmCapacity()
	//{
	//	NacSharedConstants cons = this.getSharedConstants();
	//	int size = getItemCount();
	//	//int size = this.size();

	//	return ((size+1) > cons.getMaxAlarms());
	//	//return ((size+1) <= cons.getMaxAlarms());
	//}

	/**
	 * Call the listener for when an alarm card is bound.
	 */
	public void callOnViewHolderBoundListener(NacCardHolder holder, int index)
	{
		OnViewHolderBoundListener listener = this.getOnViewHolderBoundListener();

		if (listener != null)
		{
			listener.onViewHolderBound(holder, index);
		}
	}

	/**
	 * Call the listener for when an alarm card is created.
	 */
	public void callOnViewHolderCreatedListener(NacCardHolder holder)
	{
		OnViewHolderCreatedListener listener = this.getOnViewHolderCreatedListener();

		if (listener != null)
		{
			listener.onViewHolderCreated(holder);
		}
	}

	/**
	 * @return The alarm at the given index.
	 */
	public NacAlarm getAlarmAt(int index)
	{
		return getItem(index);
	}

	/**
	 * Count the number of cards that are collapsed.
	 *
	 * @param  rv  The recyclerview containing the view holders.
	 *
	 * @return Number of cards that are collapsed.
	 */
	public int getCardsCollapsedCount(RecyclerView rv)
	{
		int size = getItemCount();
		int count = 0;

		for (int i=0; i < size; i++)
		{
			NacCardHolder card = (NacCardHolder) rv.findViewHolderForAdapterPosition(i);
			if (card.isCollapsed())
			{
				count++;
			}
		}

		return count;
	}

	/**
	 * Count the number of cards that are expanded.
	 *
	 * @param  rv  The recyclerview containing the view holders.
	 *
	 * @return Number of cards that are expanded.
	 */
	public int getCardsExpandedCount(RecyclerView rv)
	{
		List<Integer> indices = this.getIndicesOfExpandedCards(rv);
		return indices.size();
	}

	/**
	 * Get a list of the stored indices of the cards that are expanded.
	 *
	 * @return A list of the stored indices of the cards that are expanded.
	 */
	public List<Integer> getIndicesOfExpandedCards()
	{
		return this.mIndicesOfExpandedCards;
	}

	/**
	 * Get a list of the indices of the cards that are expanded.
	 *
	 * @param  rv  The recyclerview containing the view holders.
	 *
	 * @return A list of the indices of the cards that are expanded.
	 */
	public List<Integer> getIndicesOfExpandedCards(RecyclerView rv)
	{
		List<Integer> indices = new ArrayList<>();
		int size = getItemCount();

		for (int i=0; i < size; i++)
		{
			NacCardHolder card = (NacCardHolder) rv.findViewHolderForAdapterPosition(i);

			if ((card != null) && card.isExpanded())
			{
				indices.add(i);
			}
		}

		return indices;
	}

	/**
	 * @return The unique ID of an alarm. Used alongside setHasStableIds().
	 */
	@Override
	public long getItemId(int index)
	{
		NacAlarm alarm = getItem(index);
		return (alarm != null) ? alarm.getId() : RecyclerView.NO_ID;
	}

	/**
	 * @return The listener for when an alarm card is bound.
	 */
	public OnViewHolderBoundListener getOnViewHolderBoundListener()
	{
		return this.mOnViewHolderBoundListener;
	}

	/**
	 * @return The listener for when an alarm card is created.
	 */
	public OnViewHolderCreatedListener getOnViewHolderCreatedListener()
	{
		return this.mOnViewHolderCreatedListener;
	}

	/**
	 * Setup the alarm card.
	 *
	 * @param  card  The alarm card.
	 * @param  index  The position of the alarm card.
	 */
	@Override
	public void onBindViewHolder(final NacCardHolder card, int index)
	{
		NacAlarm alarm = getItem(index);
		List<Integer> expandedCards = this.getIndicesOfExpandedCards();

		card.init(alarm);

		if (expandedCards.contains(index))
		{
			card.doExpandWithColor();
		}

		this.callOnViewHolderBoundListener(card, index);
	}

	/**
	 * Create the view holder.
	 *
	 * @param  parent  The parent view.
	 * @param  viewType  The type of view.
	 */
	@NonNull
    @Override
	public NacCardHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		Context context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);
		View root = inflater.inflate(R.layout.card_frame, parent, false);
		NacCardHolder card = new NacCardHolder(root);

		this.callOnViewHolderCreatedListener(card);

		return card;
	}

	/**
	 * Set the listener for when an alarm card is bound.
	 */
	public void setOnViewHolderBoundListener(OnViewHolderBoundListener listener)
	{
		this.mOnViewHolderBoundListener = listener;
	}

	/**
	 * Set the listener for when an alarm card is created.
	 */
	public void setOnViewHolderCreatedListener(OnViewHolderCreatedListener listener)
	{
		this.mOnViewHolderCreatedListener = listener;
	}

	///**
	// * Toast for when the maximum number of alarms has been created.
	// */
	//public void toastMaxAlarmsError()
	//{
	//	Context context = this.getContext();
	//	NacSharedConstants cons = this.getSharedConstants();

	//	NacUtility.quickToast(context, cons.getErrorMessageMaxAlarms());
	//}

	public void storeIndicesOfExpandedCards(RecyclerView rv)
	{
		this.mIndicesOfExpandedCards = this.getIndicesOfExpandedCards(rv);
	}

}
