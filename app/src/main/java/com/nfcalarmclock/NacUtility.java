package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.Toast;
import java.util.Locale;

/**
 * NFC Alarm Clock Utility class.
 * 
 * Composed of static methods that can be used for various things.
 */
@SuppressWarnings({"RedundantSuppression", "UnusedReturnValue"})
public class NacUtility
{

	/**
	 * Capitalize the first letter in the string.
	 */
	public static String capitalize(String word)
	{
		Locale locale = Locale.getDefault();
		return String.format(locale, "%1$s%2$s",
			word.substring(0, 1).toUpperCase(locale),
			word.substring(1));
	}

	/**
	 * Determine the height of the view.
	 * 
	 * @return The height of the view.
	 * 
	 * @param  view  The view.
	 */
	public static int getHeight(View view)
	{
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
			view.getLayoutParams();
		int margins = lp.topMargin + lp.bottomMargin;
		int height = view.getMeasuredHeight();

		return height+margins;
	}

	/**
	 * Wrapper for Log object to print to the logcat easily.
	 */
	public static void print(String name, String string)
	{
		Log.i(name, string);
	}

	/**
	 * Print the string to logcat using this class' name.
	 */
	public static void print(String string)
	{
		NacUtility.print("NacUtility", string);
	}

	/**
	 * Format a print statement to logcat.
	 */
	public static void printf(String format, Object... args)
	{
		NacUtility.print(String.format(format, args));
	}

	/**
	 * Print to logcat with default string formatting.
	 */
	public static void printf(String string)
	{
		NacUtility.printf("%s", string);
	}

	/**
	 * Create a toast that displays for a short period of time.
	 */
	public static Toast quickToast(Context context, String message)
	{
		return NacUtility.toast(context, message, Toast.LENGTH_SHORT);
	}

	/**
	 * Convert the given string to a spanned string.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.N)
	public static Spanned toSpannedString(String message)
	{
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			? Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
			: Html.fromHtml(message);
	}

	/**
	 * Create a toast.
	 */
	public static Toast toast(Context context, String message)
	{
		return NacUtility.toast(context, message, Toast.LENGTH_LONG);
	}

	/**
	 * Create a toast.
	 */
	public static Toast toast(Context context, String message, int duration)
	{
		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
		return toast;
	}

}
