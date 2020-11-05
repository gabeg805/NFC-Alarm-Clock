package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

/**
 * A button that consists of an image to the left, and text to the right
 * of it.
 */
@SuppressWarnings("UnnecessaryInterfaceModifier")
public class NacDayButton
	extends LinearLayout
	implements View.OnClickListener,
		MaterialButtonToggleGroup.OnButtonCheckedListener
{

	/**
	 * Listener for day change events.
	 */
	public interface OnDayChangedListener
	{
		public void onDayChanged(NacDayButton button);
	}

	/**
	 * Attributes for button and text.
	 */
	public static class Attributes
	{
		public final int width;
		public final int height;
		public final String text;

		/**
		 * Initialize the attributes.
		 */
		public Attributes(Context context, AttributeSet attrs)
		{
			Resources.Theme theme = context.getTheme();
			TypedArray ta = theme.obtainStyledAttributes(attrs,
				R.styleable.NacDayButton, 0, R.style.NacDayButton);
			TypedArray androidTa = theme.obtainStyledAttributes(attrs,
				new int[] { android.R.attr.text }, 0, 0);

			try
			{
				this.width = (int) ta.getDimension(R.styleable.NacDayButton_nacWidth,
					ViewGroup.LayoutParams.WRAP_CONTENT);
				this.height = (int) ta.getDimension(R.styleable.NacDayButton_nacHeight,
					ViewGroup.LayoutParams.WRAP_CONTENT);
				this.text = androidTa.getString(0);
			}
			finally
			{
				ta.recycle();
			}
		}

	}

	/**
	 * Day button parent.
	 */
	private MaterialButtonToggleGroup mButtonToggleGroup;

	/**
	 * Day button.
	 */
	private MaterialButton mButton;

	/**
	 * View attributes.
	 */
	private Attributes mAttributes;

	/**
	 * Day changed listener.
	 */
	private NacDayButton.OnDayChangedListener mOnDayChangedListener;

	/**
	 */
	public NacDayButton(Context context)
	{
		super(context, null);
		init(null);
	}

	/**
	 */
	public NacDayButton(Context context, AttributeSet attrs)
	{
		super(context, attrs, 0);
		init(attrs);
	}

	/**
	 */
	public NacDayButton(Context context, AttributeSet attrs,
		int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	/**
	 * Disable and animate the view.
	 */
	public void disable()
	{
		this.getButton().setChecked(false);
	}

	/**
	 * Enable and animate the view.
	 */
	public void enable()
	{
		this.getButton().setChecked(true);
	}

	/**
	 * @return The button.
	 */
	public MaterialButton getButton()
	{
		return this.mButton;
	}

	/**
	 * @return The button toggle group.
	 */
	public MaterialButtonToggleGroup getButtonToggleGroup()
	{
		return this.mButtonToggleGroup;
	}

	/**
	 * @return The day attributes object.
	 */
	private Attributes getDayAttributes()
	{
		return this.mAttributes;
	}

	/**
	 * @return The day button on click listener.
	 */
	public NacDayButton.OnDayChangedListener getOnDayChangedListener()
	{
		return this.mOnDayChangedListener;
	}

	/**
	 * Initialize the view.
	 */
	public void init(AttributeSet attrs)
	{
		if (attrs == null)
		{
			return;
		}

		setOrientation(LinearLayout.HORIZONTAL);
		this.setupStyle();

		Context context = getContext();
		this.mAttributes = new Attributes(context, attrs);
		this.mOnDayChangedListener = null;

		this.getButtonToggleGroup().addOnButtonCheckedListener(this);
		setOnClickListener(this);
	}

	/**
	 */
	@Override
	public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId,
		boolean isChecked)
	{
		NacUtility.printf("onButtonChecked!");
		NacDayButton.OnDayChangedListener listener = this.getOnDayChangedListener();
		if (listener != null)
		{
			listener.onDayChanged(this);
		}
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		NacUtility.printf("onClick!");
		MaterialButton button = this.getButton();
		boolean checked = button.isChecked();

		button.setChecked(!checked);
	}

	/**
	 * Finish setting up the View.
	 */
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.setViewAttributes();
	}

	/**
	 * Set the listener for when the day is changed.
	 */
	public void setOnDayChangedListener(NacDayButton.OnDayChangedListener listener)
	{
		this.mOnDayChangedListener = listener;
	}

	/**
	 * Set the width and height of the button.
	 */
	public void setSize(int width, int height)
	{
		MaterialButton button = this.getButton();
		LayoutParams params = (LayoutParams) button.getLayoutParams();
		params.width = width;
		params.height = height;

		button.setLayoutParams(params);
	}

	/**
	 * Set the day button style.
	 */
	public void setStyle(int style)
	{
		Context context = getContext();
		LayoutInflater inflater = LayoutInflater.from(context);
		int layout = (style == 1)
			? R.layout.nac_day_button_filled
			: R.layout.nac_day_button_outlined;

		removeAllViews();
		inflater.inflate(layout, this, true);

		this.mButtonToggleGroup = findViewById(R.id.nac_day_button_group);
		this.mButton = findViewById(R.id.nac_day_button);
	}

	/**
	 * Set the text in the button.
	 *
	 * @param  text  The text to enter in the button.
	 */
	public void setText(String text)
	{
		this.getButton().setText(text);
	}

	/**
	 * Setup the style of the day button.
	 */
	protected void setupStyle()
	{
		Context context = getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int style = shared.getDayButtonStyle();

		this.setStyle(style);
	}

	/**
	 * Set view attributes.
	 */
	public void setViewAttributes()
	{
		Attributes attrs = this.getDayAttributes();

		this.setSize(attrs.width, attrs.height);
		this.setText(attrs.text);
	}

}

