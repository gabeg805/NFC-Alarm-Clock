package com.nfcalarmclock;

/**
 * @brief Adapter for when an alarm card swipe button is selected.
 */
public interface NacCardTouchHelperAdapter
{

	/**
	 * @brief Called when an alarm card should be copied.
	 */
    void onItemCopy(int pos);

	/**
	 * @brief Called when an alarm card should be deleted.
	 */
    void onItemDelete(int pos);
}
