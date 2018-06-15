package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;

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
     *          correct.
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
     * @brief Convert the id to a theme attribute color.
     * 
     * @return A theme attribute color.
     */
    public static int getThemeAttrColor(Context context,
                                        int id)
    {
        TypedValue tv = new TypedValue();
        Resources.Theme theme = context.getTheme();
        boolean success = theme.resolveAttribute(id, tv, true);
        return (tv.resourceId == 0)
            ? tv.data
            : ContextCompat.getColor(context, tv.resourceId);
    }

}
