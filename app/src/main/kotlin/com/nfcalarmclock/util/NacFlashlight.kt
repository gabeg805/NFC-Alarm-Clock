package com.nfcalarmclock.util

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

fun findCameraId(cameraManager: CameraManager): String
{
	return cameraManager.cameraIdList.firstOrNull { id ->

		// Get the camera characteristics
		val char = cameraManager.getCameraCharacteristics(id)
		//println("Face: ${char.get(CameraCharacteristics.LENS_FACING)}")
		//println("Str : ${char.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)}")

		// Check if the flashlight is available
		char.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

	} ?: ""

}
