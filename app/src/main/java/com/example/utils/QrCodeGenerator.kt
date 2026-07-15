package com.example.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

object QrCodeGenerator {
    fun generateQrCode(
        content: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        return try {
            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height
            )

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    )
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
}
