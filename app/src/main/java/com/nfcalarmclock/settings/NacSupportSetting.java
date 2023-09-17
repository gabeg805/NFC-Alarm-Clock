package com.nfcalarmclock.settings;

import android.app.Activity;
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
import com.nfcalarmclock.util.NacUtility;
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
	 * Product ID.
	 */
	public static String PRODUCT_ID_SUPPORT = "com.nfcalarmclock.support1";

	/**
	 * Billing client.
	 */
	private BillingClient mBillingClient;

	/**
	 * Activity.
	 */
	private Activity mActivity;

	/**
	 * Constructor.
	 */
	public NacSupportSetting(Activity activity)
	{
		// Build the billing client
		NacUtility.printf("Building billing client!");
		this.mBillingClient = BillingClient.newBuilder(activity)
			.setListener(this)
			.enablePendingPurchases()
			.build();

		// Set the activity
		this.mActivity = activity;
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
		this.mActivity = null;
	}

	/**
	 * Connect to Google play.
	 */
	public void connect()
	{
		BillingClient billingClient = this.getBillingClient();

		// Connect to Google Play
		NacUtility.printf("Connect to Google Play!");
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
		NacUtility.printf("consumePurchase!");
		int purchaseState = purchase.getPurchaseState();

		// The purchase did not go through
		if (purchaseState != PurchaseState.PURCHASED)
		{
			NacUtility.printf("consumePurchase! Purchase state not purchased : %d", purchaseState);
			this.cleanup();
			return;
		}

		// Build the params to consume a purchase
		ConsumeParams consumeParams = ConsumeParams.newBuilder()
			.setPurchaseToken(purchase.getPurchaseToken())
			.build();

		// Consume the purchase asynchronously
		BillingClient billingClient = this.getBillingClient();

		NacUtility.printf("consumePurchase! Officially consuming the purchase");
		billingClient.consumeAsync(consumeParams, this);
	}

	/**
	 * Get the activity.
	 *
	 * @return The activity.
	 */
	private Activity getActivity()
	{
		return this.mActivity;
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
	 * Launch the billing flow so that the user can make the purchase.
	 *
	 * @param productDetails Product details.
	 */
	private void launchBillingFlow(ProductDetails productDetails)
	{
		NacUtility.printf("launchBillingFlow!");

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
		Activity activity = this.getActivity();
		BillingClient billingClient = this.getBillingClient();

		NacUtility.printf("launchBillingFlow! Officially launching the billing flow");
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
		NacUtility.printf("Billing service disconnected!");
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
		NacUtility.printf("onBillingSetupFinished! Billing setup finished");
		// Unable to finish setup of the billing service
		if (billingResult.getResponseCode() != BillingResponseCode.OK)
		{
			NacUtility.printf("onBillingSetupFinished! Billing result is not ok! %d", billingResult.getResponseCode());

			// Cleanup the billing client
			this.cleanup();
			return;
		}

		// Query for products to sell
		NacUtility.printf("onBillingSetupFinished! Querying for products");
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
		NacUtility.printf("onConsumeResponse!");

		// Unable to consume the purchase
		if (billingResult.getResponseCode() != BillingResponseCode.OK)
		{
			NacUtility.printf("onConsumeResponse! Billing result is not ok! %d", billingResult.getResponseCode());
			// Cleanup the billing client
			this.cleanup();
			return;
		}

		// Toast to show thanks
		NacUtility.printf("onConsumeResponse! Thank you for your support!");
		NacUtility.quickToast(this.getActivity(), "Thank you for your support!");
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
		NacUtility.printf("onProductDetailsResponse! Product list size : %d", productDetailsList.size());

		// Unable to get a list of product details
		if ((billingResult.getResponseCode() != BillingResponseCode.OK)
			|| productDetailsList.isEmpty())
		{
			NacUtility.printf("onProductDetailsResponse! Billing result is not ok! %d", billingResult.getResponseCode());

			// Cleanup the billing client
			this.cleanup();
			return;
		}

		// Get the product details of the first item
		ProductDetails productDetails = productDetailsList.get(0);

		// Launch the billing flow
		NacUtility.printf("onProductDetailsResponse! Launching the billing flow");
		this.launchBillingFlow(productDetails);

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
		NacUtility.printf("onPurchasesUpdated! Purchase list size : %d", ((purchaseList != null) ? purchaseList.size() : -1));

		// Unable to complete purchase
		if ((billingResult.getResponseCode() != BillingResponseCode.OK)
			|| (purchaseList == null))
		{
			NacUtility.printf("onPurchasesUpdated! Billing result is not ok! %d", billingResult.getResponseCode());

			// Cleanup the billing client
			this.cleanup();
			return;
		}

		// Get purchase information
		Purchase purchase = purchaseList.get(0);

		// Consume the purchase
		NacUtility.printf("onPurchasesUpdated! Consume the purchase");
		this.consumePurchase(purchase);
	}

	/**
	 * Query for products to sale.
	 * <p>
	 * Note: This does a network query.
	 */
	private void queryForProducts()
	{
		NacUtility.printf("queryForProducts!");

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

		NacUtility.printf("queryForProducts! Query async");
		billingClient.queryProductDetailsAsync(queryProductDetailsParams, this);
	}

}
