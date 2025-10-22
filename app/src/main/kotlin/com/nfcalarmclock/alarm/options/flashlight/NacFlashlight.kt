package com.nfcalarmclock.alarm.options.flashlight

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.R
import com.nfcalarmclock.view.toast

/**
 * Find the first available camera ID.
 */
fun findCameraId(cameraManager: CameraManager): String
{
	return cameraManager.cameraIdList.firstOrNull { id ->

		// Get the camera characteristics
		val characteristics = cameraManager.getCameraCharacteristics(id)

		// Check if the flashlight is available
		characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

	} ?: ""

}

/**
 * Get the reason for why there was a failure to access the camera.
 */
fun getReasonForCameraAccessFailure(
	context: Context,
	exception: CameraAccessException
): String
{
	// Get the string resource to use
	val id = when (exception.reason)
	{
		CameraAccessException.CAMERA_DISABLED     -> R.string.error_message_camera_disabled
		CameraAccessException.CAMERA_DISCONNECTED -> R.string.error_message_camera_disconnected
		CameraAccessException.CAMERA_ERROR        -> R.string.error_message_camera_error
		CameraAccessException.CAMERA_IN_USE       -> R.string.error_message_camera_in_use
		CameraAccessException.MAX_CAMERAS_IN_USE  -> R.string.error_message_camera_max_in_use
		else                                      -> R.string.state_unknown
	}

	// Get the string
	return context.getString(id)
}

/**
 * Get the reason for why there was a failure to access the camera.
 */
fun toastReasonForCameraAccessFailure(
	context: Context,
	exception: CameraAccessException
)
{
	// Get the reason
	val reason = getReasonForCameraAccessFailure(context, exception)
	val message = context.getString(R.string.error_message_unable_to_access_flashlight, reason)

	// Toast
	toast(context, message)
}

/**
 * Flashlight.
 */
class NacFlashlight(private val context: Context)
{

	/**
	 * Camera manager.
	 */
	private val cameraManager: CameraManager = context.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager

	/**
	 * Camera ID.
	 */
	private val cameraId: String = try
	{
		findCameraId(cameraManager)
	}
	catch (e: CameraAccessException)
	{
		// Get the reason
		val reason = getReasonForCameraAccessFailure(context, e)
		val message = context.getString(R.string.error_message_unable_to_shine_flashlight, reason)

		// Toast
		toast(context, message)
		""
	}

	/**
	 * Camera characteristics.
	 */
	private val cameraCharacteristics: CameraCharacteristics? = if (cameraId.isNotEmpty())
	{
		try
		{
			cameraManager.getCameraCharacteristics(cameraId)
		}
		catch (e: CameraAccessException)
		{
			toastReasonForCameraAccessFailure(context, e)
			null
		}
		catch (_: IllegalArgumentException)
		{
			toast(context, R.string.error_message_unable_to_get_flashlight_characteristics)
			null
		}
	}
	else
	{
		null
	}

	/**
	 * Flag indicating whether the flashlight is running or not.
	 */
	var isRunning: Boolean = false

	/**
	 * The minimum flashlight strength level
	 */
	val minLevel: Int = 1

	/**
	 * The maximum flashlight strength level
	 */
	val maxLevel: Int
		get()
		{
			return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
			{
				cameraCharacteristics?.let {
						it.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) as Int
					} ?: 1
			}
			else
			{
				1
			}
		}

	/**
	 * The desired flashlight strength level.
	 */
	var strengthLevel: Int = maxLevel

	/**
	 * Handler to turn on the flashlight.
	 */
	private var onHandler: Handler? = null

	/**
	 * Handler to turn off the flashlight.
	 */
	private var offHandler: Handler? = null

	/**
	 * Duration to keep the flashlight on.
	 */
	private var onDuration: Long = 0

	/**
	 * Duration to keep the flashlight off.
	 */
	private var offDuration: Long = 0

	/**
	 * Blink the flashlight.
	 */
	fun blink(onTime: String, offTime: String)
	{
		// Setup the handlers
		val mainLooper = Looper.getMainLooper()
		onHandler = Handler(mainLooper)
		offHandler = Handler(mainLooper)

		// Set the durations and convert the passed in values from sec (as a String)
		// to millisecond as a Long
		onDuration = (onTime.toFloat() * 1000).toLong()
		offDuration = (offTime.toFloat() * 1000).toLong()

		// Turn on the flashlight
		turnOn()
	}

	/**
	 * Cleanup.
	 */
	fun cleanup()
	{
		// Stop any running handlers
		onHandler?.removeCallbacksAndMessages(null)
		offHandler?.removeCallbacksAndMessages(null)

		// Reset handlers
		onHandler = null
		offHandler = null

		// Reset the flag indicating the flashlight is not running
		isRunning = false

		// Turn off the flashlight
		turnOff()
	}

	/**
	 * Turn off the flashlight.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	fun turnOff()
	{
		try
		{
			// Turn off the flashlight
			cameraManager.setTorchMode(cameraId, false)
		}
		catch (e: CameraAccessException)
		{
			toastReasonForCameraAccessFailure(context, e)
		}
		catch (_: IllegalArgumentException)
		{
			toast(context, R.string.error_message_unable_to_turn_off_flashlight)
		}

		// Turn on the flashlight after the off duration has elapsed
		onHandler?.postDelayed({ turnOn() }, offDuration)
	}

	/**
	 * Turn on the flashlight.
	 */
	fun turnOn()
	{
		// Set the flag indicating the flashlight is running
		isRunning = true

		try
		{
			// Check if the flashlight strength can change
			if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && (maxLevel > 1))
			{
				// Turn on the flashlight
				cameraManager.turnOnTorchWithStrengthLevel(cameraId, strengthLevel)
			}
			else
			{
				// Turn on the flashlight
				cameraManager.setTorchMode(cameraId, true)
			}
		}
		catch (e: CameraAccessException)
		{
			toastReasonForCameraAccessFailure(context, e)
		}
		catch (_: IllegalArgumentException)
		{
			toast(context, R.string.error_message_unable_to_turn_on_flashlight)
		}

		// Turn off the flashlight after the on duration has elapsed
		offHandler?.postDelayed({ turnOff() }, onDuration)
	}

}
