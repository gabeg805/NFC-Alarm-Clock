package com.nfcalarmclock.view

import android.content.Context
import android.widget.Toast

/**
 * Create a toast that displays for a short period of time.
 */
fun quickToast(context: Context, message: String): Toast
{
	return toast(context, message, Toast.LENGTH_SHORT)
}

/**
 * Create a toast that displays for a short period of time.
 */
fun quickToast(context: Context, resId: Int): Toast
{
	// Get the message
	val message = context.getString(resId)

	// Show the toast
	return toast(context, message, Toast.LENGTH_SHORT)
}

/**
 * Create a toast.
 */
fun toast(context: Context, resId: Int): Toast
{
	// Get the message
	val message = context.getString(resId)

	// Show the toast
	return toast(context, message, Toast.LENGTH_LONG)
}

/**
 * Create a toast.
 */
fun toast(context: Context, message: String): Toast
{
	return toast(context, message, Toast.LENGTH_LONG)
}

/**
 * Create a toast.
 */
fun toast(context: Context, message: String, duration: Int): Toast
{
	// Create the toast
	val toast = Toast.makeText(context, message, duration)

	// Show the toast
	toast.show()

	return toast
}