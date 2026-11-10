package com.syedmsidsm.prayerreminder.importing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.syedmsidsm.prayerreminder.domain.OcrToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OcrEngine(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeFromUri(imageUri: Uri): List<OcrToken> = withContext(Dispatchers.IO) {
        val bitmap = loadAndPreprocessBitmap(imageUri)
        recognizeFromBitmap(bitmap)
    }

    suspend fun recognizeFromBitmap(bitmap: Bitmap): List<OcrToken> = suspendCancellableCoroutine { cont ->
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val tokens = mutableListOf<OcrToken>()
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        for (element in line.elements) {
                            val box = element.boundingBox
                            if (box != null) {
                                tokens.add(
                                    OcrToken(
                                        text = element.text,
                                        left = box.left,
                                        top = box.top,
                                        right = box.right,
                                        bottom = box.bottom
                                    )
                                )
                            }
                        }
                    }
                }
                Log.d("OcrEngine", "Recognized ${tokens.size} OCR tokens from image")
                cont.resume(tokens)
            }
            .addOnFailureListener { e ->
                Log.e("OcrEngine", "ML Kit recognition failed", e)
                cont.resumeWithException(e)
            }
    }

    private fun loadAndPreprocessBitmap(uri: Uri): Bitmap {
        var bmp: Bitmap = try {
            if (uri.scheme == "file" && uri.path != null) {
                val file = java.io.File(uri.path!!)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(file)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    BitmapFactory.decodeFile(file.absolutePath)
                        ?: throw IllegalArgumentException("Could not decode file at ${file.absolutePath}")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: throw IllegalArgumentException("Could not open stream for URI: $uri")
            }
        } catch (e: Exception) {
            Log.w("OcrEngine", "ImageDecoder failed, attempting fallback to BitmapFactory", e)
            if (uri.scheme == "file" && uri.path != null) {
                BitmapFactory.decodeFile(uri.path)
                    ?: throw IllegalArgumentException("BitmapFactory fallback could not decode file: $uri")
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    BitmapFactory.decodeStream(stream, null, options)
                } ?: throw IllegalArgumentException("Could not decode bitmap from URI: ${e.message}")
            }
        }

        // Spec 6.3: If shortest side is under 1000 px, upscale x2 (bilinear)
        val shortestSide = minOf(bmp.width, bmp.height)
        if (shortestSide < 1000) {
            val newWidth = bmp.width * 2
            val newHeight = bmp.height * 2
            bmp = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true)
        }

        Log.d("OcrEngine", "Preprocessed bitmap ready (${bmp.width}x${bmp.height})")
        return bmp
    }
}
