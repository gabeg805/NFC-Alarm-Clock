package com.nfcalarmclock.util

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import java.util.Locale

/**
 * NFC Alarm Clock Utility class.
 * <p>
 * Composed of static methods that can be used for various things.
 */
object NacUtility
{

	/**
	 * Capitalize the first letter in the string.
	 */
	fun capitalize(word: String): String
	{
		val locale = Locale.getDefault()

		return String.format(locale, "%1\$s%2\$s",
			word.substring(0, 1).uppercase(locale),
			word.substring(1))
	}

	/**
	 * Determine the height of the view.
	 *
	 * @param  view  The view.
	 *
	 * @return The height of the view.
	 */
	fun getHeight(view: View): Int
	{
		// Measure the view
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))

		// Calculate the height, including the margins
		val lp = view.layoutParams as MarginLayoutParams
		val margins = lp.topMargin + lp.bottomMargin
		val height = view.measuredHeight

		return height + margins
	}

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
	 * Convert the given string to a spanned string.
	 */
	@Suppress("deprecation")
	@TargetApi(Build.VERSION_CODES.N)
	fun toSpannedString(message: String): Spanned
	{
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			{
				Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
			}
			else
			{
				Html.fromHtml(message)
			}
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

}