package com.nfcalarmclock.alarm.card

import androidx.recyclerview.widget.ItemTouchHelper
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.card.NacBaseCardTouchHelperCallback

/**
 * Card touch helper for alarms.
 */
class NacAlarmCardTouchHelper(
	onCardSwipedListener: NacBaseCardTouchHelperCallback.OnCardSwipedListener<NacAlarm>
) : ItemTouchHelper(NacAlarmCardTouchHelperCallback(onCardSwipedListener))