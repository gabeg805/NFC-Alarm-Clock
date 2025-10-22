package com.nfcalarmclock.support

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.google.common.collect.ImmutableList
import com.nfcalarmclock.R
import com.nfcalarmclock.view.quickToast
import kotlinx.coroutines.launch

/**
 * Support setting that launches a Google billing flow.
 *
 * The flow of events is as follows:
 *
 *
 * connect()
 * onBillingSetupFinished()
 *     queryForProducts()
 * onProductDetailsResponse()
 *     launchBillingFlow()
 * onPurchasesUpdated()
 *     consumePurchase()
 * onConsumeReponse()
 */
class NacSupportSetting(

	/**
	 * Fragment activity.
	 */
	private val fragmentActivity: FragmentActivity

) : PurchasesUpdatedListener,
	BillingClientStateListener,
	ProductDetailsResponseListener,
	ConsumeResponseListener
{

	companion object
	{

		/**
		 * Product ID.
		 */
		const val PRODUCT_ID_SUPPORT = "com.nfcalarmclock.support1"

	}

	/**
	 * Listener for when a support event occurs.
	 */
	fun interface OnSupportEventListener
	{
		fun onSupported()
	}

	/**
	 * Billing client.
	 */
	private val billingClient: BillingClient = BillingClient.newBuilder(fragmentActivity)
		.setListener(this)
		.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
		.build()

	/**
	 * Listener for when a support events occurs.
	 */
	var onSupportEventListener: OnSupportEventListener? = null

	/**
	 * Consume a purchase.
	 *
	 *
	 * This allows the purchase to actually go through. If it is not consumed, Google
	 * will refund the purchase.
	 *
	 * @param purchase A purchase.
	 */
	private fun consumePurchase(purchase: Purchase)
	{
		// The purchase did not go through
		if (purchase.purchaseState != PurchaseState.PURCHASED)
		{
			// End the connection to the billing client
			billingClient.endConnection()
			return
		}

		// Build the params to consume a purchase
		val consumeParams = ConsumeParams.newBuilder()
			.setPurchaseToken(purchase.purchaseToken)
			.build()

		// Consume the purchase asynchronously
		billingClient.consumeAsync(consumeParams, this)
	}

	/**
	 * Launch the billing flow so that the user can make the purchase.
	 *
	 * @param productDetails Product details.
	 */
	private fun launchBillingFlow(activity: Activity, productDetails: ProductDetails)
	{
		// Create the params that describe the product to be purchased
		val productDetailsParamsList = ImmutableList.of(
			ProductDetailsParams.newBuilder()
				.setProductDetails(productDetails)
				.build()
		)

		// Create the params used to initiate a purchase flow
		val billingFlowParams = BillingFlowParams.newBuilder()
			.setProductDetailsParamsList(productDetailsParamsList)
			.build()

		// Launch the billing flow (ignoring the result because I do not think it is
		// necessary
		billingClient.launchBillingFlow(activity, billingFlowParams)
	}

	/**
	 * The connection to the billing service was disconnected.
	 *
	 * Note: This is part of BillingClientStateListener.
	 */
	override fun onBillingServiceDisconnected()
	{
		// End the connection to the billing client
		billingClient.endConnection()
	}

	/**
	 * Setup of the billing service is now finished.
	 *
	 *
	 * Note: This is part of BillingClientStateListener.
	 */
	override fun onBillingSetupFinished(billingResult: BillingResult)
	{
		// Unable to finish setup of the billing service
		if (billingResult.responseCode != BillingResponseCode.OK)
		{
			// Show a toast indicating there was an error
			showBillingErrorMessage()

			// End the connection to the billing client
			billingClient.endConnection()
			return
		}

		// Query for products to sell
		queryForProducts()
	}

	/**
	 * The consumption operation has finished.
	 *
	 * @param billingResult Billing result.
	 * @param purchaseToken Purchase token.
	 */
	override fun onConsumeResponse(billingResult: BillingResult, purchaseToken: String)
	{
		// Call the listener to indicate that the purchase was successfully consumed
		if (billingResult.responseCode == BillingResponseCode.OK)
		{
			// Call the support listener
			onSupportEventListener?.onSupported()
		}

		// End the connection to the billing client
		billingClient.endConnection()
	}

	/**
	 * The product details query has finished.
	 *
	 *
	 * Note: This is part of ProductDetailsResponseListener.
	 *
	 * @param billingResult Billing result.
	 * @param productDetailsResult Result of product details query.
	 */
	override fun onProductDetailsResponse(billingResult: BillingResult,
		productDetailsResult: QueryProductDetailsResult
	)
	{
		// Get the list of product details
		val productDetailsList = productDetailsResult.productDetailsList

		// Unable to get a list of product details
		if (billingResult.responseCode != BillingResponseCode.OK
			|| productDetailsList.isEmpty())
		{
			// Show a toast indicating there was an error
			showBillingErrorMessage()

			// End the connection to the billing client
			billingClient.endConnection()
			return
		}

		// Get the product details of the first item
		val productDetails = productDetailsList[0]

		// Launch billing flow, passing in the activity
		launchBillingFlow(fragmentActivity, productDetails)
	}

	/**
	 * Listener for when purchases are updated.
	 *
	 *
	 * Note: This is part of PurchasesUpdatedListener.
	 *
	 * @param billingResult Billing result.
	 * @param purchaseList List of purchases.
	 */
	override fun onPurchasesUpdated(billingResult: BillingResult,
		purchaseList: List<Purchase>?)
	{
		// Unable to complete purchase
		if ((billingResult.responseCode != BillingResponseCode.OK)
			|| (purchaseList == null))
		{
			// End the connection to the billing client
			billingClient.endConnection()
			return
		}

		// Get purchase information
		val purchase = purchaseList[0]

		// Consume the purchase
		consumePurchase(purchase)
	}

	/**
	 * Query for products to sale.
	 *
	 * Note: This does a network query.
	 */
	private fun queryForProducts()
	{
		// Parameters used to query for a list of product details
		val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
			.setProductList(
				ImmutableList.of(
					QueryProductDetailsParams.Product.newBuilder()
						.setProductId(PRODUCT_ID_SUPPORT)
						.setProductType(BillingClient.ProductType.INAPP)
						.build()))
			.build()

		// Perform a network query of the products available for sale, asynchronously
		billingClient.queryProductDetailsAsync(queryProductDetailsParams, this)
	}

	/**
	 * Show a toast when a billing error occurs.
	 */
	private fun showBillingErrorMessage()
	{
		// Make sure the following things are run on the UI thread
		fragmentActivity.lifecycleScope.launch {

			// Show a toast indicating there was an error
			quickToast(fragmentActivity, R.string.error_message_google_play_billing)

		}
	}

	/**
	 * Start the connection to the Google play billing client.
	 */
	fun start()
	{
		billingClient.startConnection(this)
	}

}