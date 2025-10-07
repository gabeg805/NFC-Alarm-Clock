package com.nfcalarmclock.timer.card

import androidx.recyclerview.widget.ItemTouchHelper
import com.nfcalarmclock.card.NacBaseCardTouchHelperCallback
import com.nfcalarmclock.timer.db.NacTimer

/**
 * Card touch helper for timers.
 */
class NacTimerCardTouchHelper(
	onCardSwipedListener: NacBaseCardTouchHelperCallback.OnCardSwipedListener<NacTimer>
) : ItemTouchHelper(NacBaseCardTouchHelperCallback(onCardSwipedListener))