package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

public class NacUtility
{

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
