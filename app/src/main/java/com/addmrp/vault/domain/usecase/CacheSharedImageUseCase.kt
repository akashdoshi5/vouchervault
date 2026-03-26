package com.addmrp.vault.domain.usecase

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copies an external shared image Uri to the app's internal cache.
 * Implements Rule 19: "Uri Permission Safety" to prevent SecurityException
 * if the originating app closes before OCR completes.
 */
@Singleton
class CacheSharedImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun execute(uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream == null) return@withContext null

            val cacheDir = File(context.cacheDir, "shared_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            // Create a unique file in the cache directory
            val cachedFile = File(cacheDir, "shared_img_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(cachedFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(cachedFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
