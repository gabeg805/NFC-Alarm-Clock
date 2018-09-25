package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.util.Log;
import android.util.TypedValue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @brief NFC Alarm Clock Utility class.
 * 
 * @details Composed of static methods that can be used for various things.
 */
public class NacUtility
{

	/**
	 * @brief Wrapper for Log object to print to the logcat easily.
	 */
	public static void print(String name, String string)
	{
		Log.i(name, string);

		//File logFile = new File("/storage/emulated/0/log.file");

		//if (!logFile.exists())
		//{
		//	try
		// 	{
		//		logFile.createNewFile();
		//	}
		//	catch (IOException e)
		//	{
		//		e.printStackTrace();
		//	}
		//}

		//try
		//{
		//	//BufferedWriter for performance, true to set append to file flag
		//	BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
		//		true));

		//	buf.append(string);
		//	buf.newLine();
		//	buf.close();
		//}
		//catch (IOException e)
		//{
		//	e.printStackTrace();
		//}
	}

	/**
	 * @brief Print the string to logcat using this class' name.
	 */
	public static void print(String string)
	{
		NacUtility.print("NacUtility", string);
	}

	/**
	 * @brief Format a print statement to logcat.
	 * 
	 * @details Can easily print any objects as long as the format string is
	 *			correct.
	 */
	public static void printf(String format, Object... args)
	{
		NacUtility.print(String.format(format, args));
	}

	/**
	 * @brief Print to logcat with default string formatting.
	 */
	public static void printf(String string)
	{
		NacUtility.printf("%s", string);
	}

	/**
	 * @brief Set background color of the view.
	 */
	public static void setBackground(Context c, View v, int id)
	{
		int bg = NacUtility.getThemeAttrColor(c, id);

		v.setBackground(null);
		v.setBackgroundColor(bg);
	}

	/**
	 * @brief Convert the id to a theme attribute color.
	 * 
	 * @return A theme attribute color.
	 */
	public static int getThemeAttrColor(Context c, int id)
	{
		TypedValue tv = new TypedValue();
		Resources.Theme theme = c.getTheme();
		boolean success = theme.resolveAttribute(id, tv, true);

		return (tv.resourceId == 0)
			? tv.data
			: ContextCompat.getColor(c, tv.resourceId);
	}

	/**
	 * @brief Determine the height of the view.
	 * 
	 * @param  v  The view.
	 * 
	 * @return The height of the view.
	 */
	public static int getHeight(View v)
	{
		v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
			v.getLayoutParams();
		int margins = lp.topMargin + lp.bottomMargin;
		int height = v.getMeasuredHeight();

		return height+margins;
	}

}
