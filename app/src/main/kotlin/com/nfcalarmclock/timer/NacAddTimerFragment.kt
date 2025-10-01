package com.nfcalarmclock.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Add a timer.
 */
@AndroidEntryPoint
class NacAddTimerFragment
	: Fragment()
{

	/**
	 * Called to create the root view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.frg_add_timer, container, false)
	}

	/**
	 * Called when the activity is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Setup
		super.onViewCreated(view, savedInstanceState)

		//// Hour label
		//val hourLabel: TextView = view.findViewById(R.id.timer_hour)

		//// Numberpad buttons
		//val numpad1: MaterialButton = view.findViewById(R.id.timer_numberpad1)
		//val numpad2: MaterialButton = view.findViewById(R.id.timer_numberpad2)
		//val numpad3: MaterialButton = view.findViewById(R.id.timer_numberpad3)
		//val numpad4: MaterialButton = view.findViewById(R.id.timer_numberpad4)
		//val numpad5: MaterialButton = view.findViewById(R.id.timer_numberpad5)
		//val numpad6: MaterialButton = view.findViewById(R.id.timer_numberpad6)
		//val numpad7: MaterialButton = view.findViewById(R.id.timer_numberpad7)
		//val numpad8: MaterialButton = view.findViewById(R.id.timer_numberpad8)
		//val numpad9: MaterialButton = view.findViewById(R.id.timer_numberpad9)
		//val numpadInvis: MaterialButton = view.findViewById(R.id.timer_numberpad_invis)
		//val numpad0: MaterialButton = view.findViewById(R.id.timer_numberpad0)
		//val numpadDel: MaterialButton = view.findViewById(R.id.timer_numberpad_del)

		//// Start button
		//// TODO: Make it greyed out if no time selected? Or maybe just don't do anything?
		//// TODO: Change icon color based on theme color
		//val startButton: MaterialButton = view.findViewById(R.id.timer_start)

		//// Get the height the hour label will take up
		//hourLabel.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
		//	override fun onGlobalLayout()
		//	{
		//		println("HELLO THERE! : ${hourLabel.height} | ${hourLabel.measuredHeight} | ${hourLabel.paddingTop} x ${hourLabel.paddingBottom}")
		//		// Get the height of everything
		//		val screenHeight = resources.displayMetrics.heightPixels
		//		val hourHeight = hourLabel.height + hourLabel.paddingTop + hourLabel.paddingBottom
		//		val rootPadding = resources.getDimensionPixelSize(R.dimen.normal) * 2
		//		val numberpadMargin = resources.getDimensionPixelSize(R.dimen.tiny) * 2 * 4
		//		val startMargin = resources.getDimensionPixelSize(R.dimen.huge) * 2
		//		val availableHeight = screenHeight - (hourHeight + rootPadding + startMargin + numberpadMargin)
		//		val newNumpadHeight = availableHeight / 5

		//		println("Screen height : $screenHeight | Available : $availableHeight")
// scree//n size - (padding normal + height of timer_hour + height of timer_start (make sure heights include margin and if not, add them)) / 4

		//		numpad1.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpad2.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpad3.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpad4.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpad5.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpad6.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpad7.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpad8.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpad9.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpadInvis.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpad0.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		numpadDel.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		startButton.updateLayoutParams {
		//			height = newNumpadHeight
		//			width = newNumpadHeight
		//		}

		//		// Remove to prevent multiple callbacks
		//		hourLabel.viewTreeObserver.removeOnGlobalLayoutListener(this)
		//	}
		//})

	}

	override fun onResume()
	{
		super.onResume()

		val view = requireView()

		//// Hour label
		//val hourLabel: TextView = view.findViewById(R.id.timer_hour)
		//hourLabel.updateLayoutParams<ViewGroup.MarginLayoutParams> {
		//	println("HELLO : $topMargin x $bottomMargin")
		//}

		//// Numberpad buttons
		//val numpad1: MaterialButton = view.findViewById(R.id.timer_numberpad1)
		//val numpad2: MaterialButton = view.findViewById(R.id.timer_numberpad2)
		//val numpad3: MaterialButton = view.findViewById(R.id.timer_numberpad3)
		//val numpad4: MaterialButton = view.findViewById(R.id.timer_numberpad4)
		//val numpad5: MaterialButton = view.findViewById(R.id.timer_numberpad5)
		//val numpad6: MaterialButton = view.findViewById(R.id.timer_numberpad6)
		//val numpad7: MaterialButton = view.findViewById(R.id.timer_numberpad7)
		//val numpad8: MaterialButton = view.findViewById(R.id.timer_numberpad8)
		//val numpad9: MaterialButton = view.findViewById(R.id.timer_numberpad9)
		//val numpad0: MaterialButton = view.findViewById(R.id.timer_numberpad0)
		//val numpadDel: MaterialButton = view.findViewById(R.id.timer_numberpad_del)

		//// Start button
		//// TODO: Make it greyed out if no time selected? Or maybe just don't do anything?
		//// TODO: Change icon color based on theme color
		//val startButton: MaterialButton = view.findViewById(R.id.timer_start)

		//// Get the size of the screen
		//val screenHeight = resources.displayMetrics.heightPixels

		//println("Height : $screenHeight | Hour height : ${hourLabel.height} x ${hourLabel.measuredHeight} | Margin : ${hourLabel.marginTop} x ${hourLabel.marginBottom}")
	}
}