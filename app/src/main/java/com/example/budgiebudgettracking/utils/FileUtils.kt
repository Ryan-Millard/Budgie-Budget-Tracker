package com.example.budgiebudgettracking.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object FileUtils {

	fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
		return try {
			val contentResolver = context.contentResolver
			val inputStream = contentResolver.openInputStream(uri)
			val fileName = getFileName(contentResolver, uri)
			val file = File(context.filesDir, fileName)
			val outputStream = FileOutputStream(file)
			inputStream?.copyTo(outputStream)
			inputStream?.close()
			outputStream.close()
			file.absolutePath
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}

	private fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
		var name = "profile_image"
		val cursor = contentResolver.query(uri, null, null, null, null)
		cursor?.use {
			val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
			if (it.moveToFirst()) {
				name = it.getString(nameIndex)
			}
		}
		return name
	}
}
