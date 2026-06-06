package com.example.homeserv.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Copies a picked image URI into the app's private storage folder.
 * Returns the absolute path of the saved file, or null on failure.
 *
 * Usage:
 *   val path = ImageHelper.saveImage(context, uri, "providers", "provider_123.jpg")
 *   Glide.with(ctx).load(File(path)).into(imageView)
 */
object ImageHelper {

    /**
     * Saves a content URI to app private storage.
     * @param context   application context
     * @param uri       content URI from image picker
     * @param folder    subfolder name inside filesDir (e.g. "providers", "categories")
     * @param fileName  desired file name (e.g. "provider_abc123.jpg")
     * @return absolute file path string, or null if save failed
     */
    fun saveImage(
        context: Context,
        uri: Uri,
        folder: String,
        fileName: String
    ): String? {
        return try {
            val dir = File(context.filesDir, folder).also { it.mkdirs() }
            val dest = File(dir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
            dest.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Returns a File object from a stored path string.
     * Returns null if path is empty or file doesn't exist.
     */
    fun getFile(path: String?): File? {
        if (path.isNullOrEmpty()) return null
        val f = File(path)
        return if (f.exists()) f else null
    }

    /**
     * Deletes a locally stored image by path.
     */
    fun deleteImage(path: String?) {
        path?.let { File(it).delete() }
    }

    /**
     * Generates a unique file name for an image.
     * e.g. "provider_1698765432100.jpg"
     */
    fun generateFileName(prefix: String): String =
        "${prefix}_${System.currentTimeMillis()}.jpg"
}