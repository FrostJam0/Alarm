package com.alarm.app.core.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.provider.MediaStore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Utility object responsible for generating QR codes and saving them to the device's gallery.
 */
object QrCodeGenerator {

    /**
     * Generates a QR code bitmap from the given string content.
     *
     * @param content The string content to encode into the QR code.
     * @param size The width and height (in pixels) of the generated QR code. Defaults to 512.
     * @return A [Bitmap] representing the QR code, or null if generation fails or content is empty.
     */
    fun generate(content: String, size: Int = 512): Bitmap? {
        if (content.isEmpty()) return null
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a generated QR code [Bitmap] to the device's public image gallery.
     *
     * @param context The application or activity context.
     * @param bitmap The QR code bitmap to save.
     * @param name The name to use for the saved file (without extension).
     * @return True if the image was successfully saved, false otherwise.
     */
    fun saveQrToGallery(context: Context, bitmap: Bitmap, name: String): Boolean {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "QR_$name.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Alarm")
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return false

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(uri, null, null)
            false
        }
    }
}
