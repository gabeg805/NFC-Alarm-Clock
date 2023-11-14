package com.nfcalarmclock.support

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.common.collect.ImmutableList

/**
 * Support setting.
 *
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
	 * Context.
	 */
	context: Context

) : PurchasesUpdatedListener,
	BillingClientStateListener,
	ProductDetailsResponseListener,
	ConsumeResponseListener
{

	/**
	 * Listener for when billing events occur.
	 */
	interface OnBillingEventListener
	{
		fun onBillingError()
		fun onPrepareToLaunchBillingFlow(productDetails: ProductDetails)
		fun onSupportPurchased()
	}

	/**
	 * Billing client.
	 */
	private val billingClient: BillingClient = BillingClient.newBuilder(context)
		.setListener(this)
		.enablePendingPurchases()
		.build()

	/**
	 * Listener for billing events.
	 */
	var onBillingEventListener: OnBillingEventListener? = null

	/**
	 * Call the listener when there is a billing error.
	 */
	private fun callOnBillingError()
	{
		onBillingEventListener?.onBillingError()
	}

	/**
	 * Call the listener when preparing to launch billing flow.
	 *
	 * @param productDetails Product details.
	 */
	private fun callOnPrepareToLaunchBillingFlow(productDetails: ProductDetails)
	{
		onBillingEventListener?.onPrepareToLaunchBillingFlow(productDetails)
	}

	/**
	 * Call the listener to indicate that the purchase was successfully consumed.
	 */
	private fun callOnSupportPurchased()
	{
		onBillingEventListener?.onSupportPurchased()
	}

	/**
	 * Cleanup the billing client.
	 */
	private fun cleanup()
	{
		// End the connection to the billing client
		billingClient.endConnection()
	}

	/**
	 * Connect to Google play.
	 */
	fun connect()
	{
		billingClient.startConnection(this)
	}

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
			// Cleanup the billing client
			cleanup()
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
	fun launchBillingFlow(activity: Activity, productDetails: ProductDetails)
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
	 *
	 * Note: This is part of BillingClientStateListener.
	 */
	override fun onBillingServiceDisconnected()
	{
		// Cleanup the billing client
		cleanup()
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
			// Call the listener to indicate that there was a billing error
			callOnBillingError()

			// Cleanup the billing client
			cleanup()
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
			callOnSupportPurchased()
		}

		// Cleanup the billing client
		cleanup()
	}

	/**
	 * The product details query has finished.
	 *
	 *
	 * Note: This is part of ProductDetailsResponseListener.
	 *
	 * @param billingResult Billing result.
	 * @param productDetailsList List of product details.
	 */
	override fun onProductDetailsResponse(billingResult: BillingResult,
		productDetailsList: List<ProductDetails>)
	{
		// Unable to get a list of product details
		if (billingResult.responseCode != BillingResponseCode.OK
			|| productDetailsList.isEmpty())
		{
			// Call the listener to indicate that there was a billing error
			callOnBillingError()

			// Cleanup the billing client
			cleanup()
			return
		}

		// Get the product details of the first item
		val productDetails = productDetailsList[0]

		// Call the listener indicating that the app is ready to launch the billing flow
		callOnPrepareToLaunchBillingFlow(productDetails)
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
			// Cleanup the billing client
			cleanup()
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

	companion object
	{

		/**
		 * Product ID.
		 */
		const val PRODUCT_ID_SUPPORT = "com.nfcalarmclock.support1"

	}

}