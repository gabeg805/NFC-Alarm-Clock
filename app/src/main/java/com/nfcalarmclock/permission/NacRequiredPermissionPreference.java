package com.nfcalarmclock.permission;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.nfcalarmclock.R;

/**
 * A preference that is used to display optional permissions.
 */
public class NacRequiredPermissionPreference
	extends Preference
{

	/**
	 */
	public NacRequiredPermissionPreference(Context context)
	{
		super(context);
		this.init();
	}

	/**
	 */
	public NacRequiredPermissionPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init();
	}

	/**
	 */
	public NacRequiredPermissionPreference(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		this.init();
	}

	/**
	 * Initialize the preference.
	 */
	private void init()
	{
		setLayoutResource(R.layout.nac_preference_permission);
	}

	/**
	 */
	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);

		Context context = getContext();
		Resources res = context.getResources();
		TextView permissionType = (TextView) holder.findViewById(R.id.permission_type);

		// Get the text and color
		String text = res.getString(R.string.message_required).toLowerCase();
		int color = res.getColor(R.color.red);

		// Setup the textview
		permissionType.setText(text);
		permissionType.setTextColor(color);
	}

}
