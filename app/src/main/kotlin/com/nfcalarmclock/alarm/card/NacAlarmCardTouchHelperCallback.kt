package com.nfcalarmclock.alarm.card

import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.card.NacBaseCardTouchHelperCallback

class NacAlarmCardTouchHelperCallback(
	onCardSwipedListener: OnCardSwipedListener<NacAlarm>
) : NacBaseCardTouchHelperCallback<NacAlarm>(onCardSwipedListener)