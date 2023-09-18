package com.nfcalarmclock.settings;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchaseState;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Support setting.
 * <p>
 * The flow of events is as follows:
 * <p>
 *     connect()
 *     onBillingSetupFinished()
 *         queryForProducts()
 *     onProductDetailsResponse()
 *         launchBillingFlow()
 *     onPurchasesUpdated()
 *         consumePurchase()
 *     onConsumeReponse()
 */
public class NacSupportSetting
	implements PurchasesUpdatedListener,
		BillingClientStateListener,
		ProductDetailsResponseListener,
		ConsumeResponseListener
{

	/**
	 * Listener for when billing events occur.
	 */
	public interface OnBillingEventListener
	{
		void onBillingError();
		void onPrepareToLaunchBillingFlow(ProductDetails productDetails);
		void onSupportPurchased();
	}

	/**
	 * Product ID.
	 */
	public static String PRODUCT_ID_SUPPORT = "com.nfcalarmclock.support1";

	/**
	 * Billing client.
	 */
	private BillingClient mBillingClient;

	/**
	 * Listener for billing events.
	 */
	private OnBillingEventListener mOnBillingEventListener;

	/**
	 * Constructor.
	 */
	public NacSupportSetting(Context context)
	{
		// Build the billing client
		this.mBillingClient = BillingClient.newBuilder(context)
			.setListener(this)
			.enablePendingPurchases()
			.build();

		// Set the billing event listener
		this.mOnBillingEventListener = null;
	}

	/**
	 * Call the listener when there is a billing error.
	 */
	private void callOnBillingError()
	{
		OnBillingEventListener listener = this.getOnBillingEventListener();

		// Make sure the listener has been set
		if (listener != null)
		{
			listener.onBillingError();
		}
	}

	/**
	 * Call the listener when preparing to launch billing flow.
	 *
	 * @param productDetails Product details.
	 */
	private void callOnPrepareToLaunchBillingFlow(ProductDetails productDetails)
	{
		OnBillingEventListener listener = this.getOnBillingEventListener();

		// Make sure the listener has been set
		if (listener != null)
		{
			listener.onPrepareToLaunchBillingFlow(productDetails);
		}

	}

	/**
	 * Call the listener to indicate that the purchase was successfully consumed.
	 */
	private void callOnSupportPurchased()
	{

		OnBillingEventListener listener = this.getOnBillingEventListener();

		// Make sure the listener has been set
		if (listener != null)
		{
			listener.onSupportPurchased();
		}
	}

	/**
	 * Cleanup the billing client.
	 */
	public void cleanup()
	{
		// End the connection to the billing client
		this.getBillingClient().endConnection();

		// Cleanup the member variables
		this.mBillingClient = null;
		this.mOnBillingEventListener = null;
	}

	/**
	 * Connect to Google play.
	 */
	public void connect()
	{
		BillingClient billingClient = this.getBillingClient();

		// Connect to Google Play
		billingClient.startConnection(this);
	}

	/**
	 * Consume a purchase.
	 * <p>
	 * This allows the purchase to actually go through. If it is not consumed, Google
	 * will refund the purchase.
	 *
	 * @param purchase A purchase.
	 */
	private void consumePurchase(Purchase purchase)
	{
		int purchaseState = purchase.getPurchaseState();

		// The purchase did not go through
		if (purchaseState != PurchaseState.PURCHASED)
		{
			// Cleanup the billing client
			this.cleanup();
			return;
		}

		// Build the params to consume a purchase
		ConsumeParams consumeParams = ConsumeParams.newBuilder()
			.setPurchaseToken(purchase.getPurchaseToken())
			.build();

		// Consume the purchase asynchronously
		BillingClient billingClient = this.getBillingClient();

		billingClient.consumeAsync(consumeParams, this);
	}

	/**
	 * Get the billing client.
	 *
	 * @return The billing client.
	 */
	public BillingClient getBillingClient()
	{
		return this.mBillingClient;
	}

	/**
	 * Get the billing event listener.
	 *
	 * @return The billing event listener.
	 */
	private OnBillingEventListener getOnBillingEventListener()
	{
		return this.mOnBillingEventListener;
	}

	/**
	 * Launch the billing flow so that the user can make the purchase.
	 *
	 * @param productDetails Product details.
	 */
	public void launchBillingFlow(Activity activity, ProductDetails productDetails)
	{
		// Create the params that describe the product to be purchased
		ImmutableList<ProductDetailsParams> productDetailsParamsList = ImmutableList.of(
			BillingFlowParams.ProductDetailsParams.newBuilder()
				.setProductDetails(productDetails)
				.build()
		);

		// Create the params used to initiate a purchase flow
		BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
			.setProductDetailsParamsList(productDetailsParamsList)
			.build();

		// Launch the billing flow (ignoring the result because I do not think it is
		// necessary
		BillingClient billingClient = this.getBillingClient();

		billingClient.launchBillingFlow(activity, billingFlowParams);
	}

	/**
	 * The connection to the billing service was disconnected.
	 * <p>
	 * Note: This is part of BillingClientStateListener.
	 */
	@Override
	public void onBillingServiceDisconnected()
	{
		// Cleanup the billing client
		this.cleanup();
	}

	/**
	 * Setup of the billing service is now finished.
	 * <p>
	 * Note: This is part of BillingClientStateListener.
	 */
	@Override
	public void onBillingSetupFinished(@NonNull BillingResult billingResult)
	{
		// Unable to finish setup of the billing service
		if (billingResult.getResponseCode() != BillingResponseCode.OK)
		{
			// Call the listener to indicate that there was a billing error
			this.callOnBillingError();

			// Cleanup the billing client
			this.cleanup();

			return;
		}

		// Query for products to sell
		this.queryForProducts();
	}

	/**
	 * The consumption operation has finished.
	 *
	 * @param billingResult Billing result.
	 * @param purchaseToken Purchase token.
	 */
	@Override
	public void onConsumeResponse(BillingResult billingResult, String purchaseToken)
	{
		// Call the listener to indicate that the purchase was successfully consumed
		if (billingResult.getResponseCode() == BillingResponseCode.OK)
		{
			this.callOnSupportPurchased();
		}

		// Cleanup the billing client
		this.cleanup();
	}

	/**
	 * The product details query has finished.
	 * <p>
	 * Note: This is part of ProductDetailsResponseListener.
	 *
	 * @param billingResult Billing result.
	 * @param productDetailsList List of product details.
	 */
	@Override
	public void onProductDetailsResponse(@NonNull BillingResult billingResult,
		@NonNull List<ProductDetails> productDetailsList)
	{
		// Unable to get a list of product details
		if ((billingResult.getResponseCode() != BillingResponseCode.OK)
			|| productDetailsList.isEmpty())
		{
			// Call the listener to indicate that there was a billing error
			this.callOnBillingError();

			// Cleanup the billing client
			this.cleanup();

			return;
		}

		// Get the product details of the first item
		ProductDetails productDetails = productDetailsList.get(0);

		// Call the listener indicating that the app is ready to launch the billing flow
		this.callOnPrepareToLaunchBillingFlow(productDetails);
	}

	/**
	 * Listener for when purchases are updated.
	 * <p>
	 * Note: This is part of PurchasesUpdatedListener.
	 *
	 * @param billingResult Billing result.
	 * @param purchaseList List of purchases.
	 */
	@Override
	public void onPurchasesUpdated(@NonNull BillingResult billingResult,
		@Nullable List<Purchase> purchaseList)
	{
		// Unable to complete purchase
		if ((billingResult.getResponseCode() != BillingResponseCode.OK)
			|| (purchaseList == null))
		{
			// Cleanup the billing client
			this.cleanup();
			return;
		}

		// Get purchase information
		Purchase purchase = purchaseList.get(0);

		// Consume the purchase
		this.consumePurchase(purchase);
	}

	/**
	 * Query for products to sale.
	 * <p>
	 * Note: This does a network query.
	 */
	private void queryForProducts()
	{
		// Parameters used to query for a list of product details
		QueryProductDetailsParams queryProductDetailsParams =
			QueryProductDetailsParams.newBuilder()
				.setProductList(
					ImmutableList.of(
						QueryProductDetailsParams.Product.newBuilder()
							.setProductId(PRODUCT_ID_SUPPORT)
							.setProductType(BillingClient.ProductType.INAPP)
							.build()))
				.build();

		// Perform a network query of the products available for sale, asynchronously
		BillingClient billingClient = this.getBillingClient();

		billingClient.queryProductDetailsAsync(queryProductDetailsParams, this);
	}

	/**
	 * Set the billing event listener
	 *
	 * @param listener The billing event listener.
	 */
	public void setOnBillingEventListener(OnBillingEventListener listener)
	{
		this.mOnBillingEventListener = listener;
	}

}
