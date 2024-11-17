package com.nfcalarmclock.system.file

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Unzip a file to directory.
 */
fun unzipFile(
	inputStream: InputStream,
	outputDirectory: File
): List<String>
{
	val unzippedFiles = mutableListOf<String>()

	// Open the zip file
	ZipInputStream(BufferedInputStream(inputStream)).use { zipInput ->

		// Iterate over each entry in the zip file
		while (true)
		{
			val entry = zipInput.nextEntry ?: break
			val path = "${outputDirectory.path}/${entry.name}"

			// Unzip the entry and copy its data to the specified path
			BufferedOutputStream(FileOutputStream(path)).use { outputStream ->
				zipInput.copyTo(outputStream, 1024)
			}

			// Add the path of the unzipped file to the list
			unzippedFiles.add(path)

		}

	}

	return unzippedFiles
}

/**
 * Zip a list of files.
 */
fun zipFiles(
	outputStream: OutputStream,
	files: List<File>
)
{
	// Start the zip process
	ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOutput ->

		// Iterate over each file
		for (f in files)
		{
			// Check if the file exists
			if (!f.exists())
			{
				continue
			}

			// Create a zip entry for a file
			val zipEntry = ZipEntry(f.name)

			// Add the zip entry to the zip file
			zipOutput.putNextEntry(zipEntry)

			// Copy the contents of the file to the zip file, which I
			// believe will be written to the previously set zip entry
			BufferedInputStream(FileInputStream(f)).use { inputStream ->
				inputStream.copyTo(zipOutput, 1024)
			}
		}

	}
}
