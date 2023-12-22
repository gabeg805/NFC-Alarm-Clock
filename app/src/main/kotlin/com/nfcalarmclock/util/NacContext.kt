package com.nfcalarmclock.util

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Receiver for the time tick intent. This is called when the time increments
 * every minute.
 */
fun createTimeTickReceiver(
	listener: (Context, Intent) -> Unit
): BroadcastReceiver
{
	return object : BroadcastReceiver()
	{

		/**
		 * Called when the broadcast is received.
		 */
		override fun onReceive(context: Context, intent: Intent)
		{
			listener(context, intent)
		}

	}
}

/**
 * Disable the alias for the main activity so that tapping an NFC tag
 * DOES NOT open the main activity.
 */
fun disableActivityAlias(context: Context)
{
	// Build the component name
	val aliasName = "${context.packageName}.main.NacMainAliasActivity"
	val componentName = ComponentName(context, aliasName)

	// Disable the alias
	context.packageManager.setComponentEnabledSetting(componentName,
		PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		PackageManager.DONT_KILL_APP)
}

/**
 * Enable alias for the main activity so that tapping an NFC tag will open
 * the main activity.
 */
fun enableActivityAlias(context: Context)
{
	// Build the component name
	val aliasName = "${context.packageName}.main.NacMainAliasActivity"
	val componentName = ComponentName(context, aliasName)

	// Enable the alias
	context.packageManager.setComponentEnabledSetting(componentName,
		PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		PackageManager.DONT_KILL_APP)
}

/**
 * Register a broadcast receiver.
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun registerMyReceiver(
	context: Context,
	broadcastReceiver: BroadcastReceiver,
	intentFilter: IntentFilter)
{
	// Register the receiver
	context.registerReceiver(broadcastReceiver, intentFilter)
}

/**
 * Unregister a broadcast receiver.
 */
fun unregisterMyReceiver(context: Context, broadcastReceiver: BroadcastReceiver)
{
	try
	{
		// Unregister the receiver
		context.unregisterReceiver(broadcastReceiver)
	}
	catch (_: IllegalArgumentException)
	{
	}
}

/**
 * Extension function to have a broadcast receiver execute something asynchronously.
 */
fun BroadcastReceiver.goAsync(
	context: CoroutineContext = EmptyCoroutineContext,
	block: suspend CoroutineScope.() -> Unit
)
{
	val pendingResult = goAsync()

	// Must run globally; there's no teardown callback.
	@OptIn(DelicateCoroutinesApi::class)
	GlobalScope.launch(context) {
		try
		{
			block()
		}
		finally
		{
			pendingResult.finish()
		}
	}

}
